package com.example.personalJobs.service;

import com.example.personalJobs.dto.ApplyRequest;
import com.example.personalJobs.entity.Certificate;
import com.example.personalJobs.entity.Resume;
import com.example.personalJobs.entity.CareerHistory;
import com.example.personalJobs.entity.ResumeAttachment;          // ✅ M113
import com.example.personalJobs.entity.ResumeFileAttachment;      // ✅ M115
import com.example.personalJobs.entity.ServiceProofAttachment;    // ✅ M114
import com.example.personalJobs.repository.CareerHistoryRepository;
import com.example.personalJobs.repository.ResumeAttachmentRepository; // ✅ M113 repo
import com.example.personalJobs.repository.ResumeFileAttachmentRepository;
import com.example.personalJobs.repository.ResumeRepository;
import com.example.personalJobs.repository.ServiceProofAttachmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final CareerHistoryRepository careerHistoryRepository;

    // ✅ 첨부 테이블 Repo
    private final ResumeAttachmentRepository resumeAttachmentRepository; // ✅ M113
    private final ServiceProofAttachmentRepository serviceProofAttachmentRepository; // ✅ M114
    private final ResumeFileAttachmentRepository resumeFileAttachmentRepository;     // ✅ M115

    private final ObjectMapper om = new ObjectMapper();

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    private static final String FILETYPE_PHOTO = "PHOTO";       // ✅ 사진(M113)

    @Transactional(readOnly = true)
    public Optional<ApplyRequest> getLatestResumeAsApplyRequest(Integer seqNoM100) {
        Long m100 = seqNoM100.longValue();

        return resumeRepository.findTopBySeqNoM100AndDelYnOrderBySeqNoM110Desc(m100, "N")
                .map(this::toApplyRequestFull);
    }

    @Transactional(readOnly = true)
    public Optional<ApplyRequest> getResumeAsApplyRequestByM110(Long seqNoM110) {
        return resumeRepository.findById(seqNoM110)
                .filter(r -> "N".equalsIgnoreCase(r.getDelYn()))
                .map(this::toApplyRequestFull);
    }

    @Transactional
    public Resume saveResumeFromApplyRequest(Integer seqNoM100, ApplyRequest req) {

        Resume r = new Resume();
        r.setSeqNoM100(seqNoM100);
        r.setDelYn("N");

        // ===== 기본 =====
        r.setName(req.getName());
        r.setGender(req.getGender());
        r.setBirthDate(req.getBirthDate());
        r.setPhone(req.getPhone());
        r.setEmail(req.getEmail());
        r.setAddress(req.getAddress());

        // 최종학력
        r.setSchoolName(req.getSchool());
        r.setMajor(req.getMajor());
        r.setEntranceDate(req.getEnrollDate());
        r.setGradDate(req.getGraduateDate());
        r.setScore(req.getGpa());
        r.setGradStatus(req.getGraduateStatus());

        // 기술 / 자기소개
        r.setSpeciality(req.getSkill());
        r.setIntroduction(req.getSelfIntro());

        // ===== 경력(M111 저장용 리스트) =====
        // ✅ careerDesc(JSON) 컬럼은 더 이상 사용하지 않음
        List<ApplyRequest.Career> careers =
                (req.getCareers() != null) ? req.getCareers() : req.toCareers();

        // ===== 자격증 =====
        List<ApplyRequest.License> licenses = (req.getLicenses() != null) ? req.getLicenses() : req.toLicenses();
        List<Certificate> certs = new ArrayList<>();
        if (licenses != null) {
            for (ApplyRequest.License l : licenses) {
                boolean allBlank =
                        isBlank(l.getCertificateNm()) &&
                                isBlank(l.getObtainDate()) &&
                                isBlank(l.getAgency()) &&
                                isBlank(l.getCertificateNum());
                if (allBlank) continue;

                Certificate c = new Certificate();
                c.setResume(r);
                c.setDelYn("N");
                c.setCertificateNm(l.getCertificateNm());
                c.setObtainDate(l.getObtainDate());
                c.setAgency(l.getAgency());
                c.setCertificateNum(l.getCertificateNum());
                certs.add(c);
            }
        }
        r.setCertificates(certs);

        // ✅ Resume 먼저 저장해서 seq_no_m110 확보
        Resume saved = resumeRepository.save(r);

        // =========================
        // ✅ 파일 저장 (M110에 저장 안 함)
        // =========================

        // 1) 사진: M113 (ResumeAttachment, fileType=PHOTO)
        // ✅✅✅ 수정: 새 파일이 없으면 req.photoPath(기존경로)를 그대로 새 Resume에 복사해서 insert
        String newPhotoPath = saveFileAsPath(req.getATTACH_FILE_NM_M113(), "photos");
        String effectivePhotoPath = (newPhotoPath != null) ? newPhotoPath : safe(req.getPhotoPath());
        if (effectivePhotoPath != null) {
            ResumeAttachment photo = new ResumeAttachment();
            photo.setResume(saved);
            photo.setFileType(FILETYPE_PHOTO);
            photo.setFilePath(effectivePhotoPath);
            photo.setFileName(null);
            photo.setFileSize(null);
            photo.setUploadDate(LocalDateTime.now().toString());
            photo.setDelYn("N");
            resumeAttachmentRepository.save(photo);
        }

        // 2) 복무증명서: M114
        // ✅✅✅ 수정: 새 파일이 없으면 req.serviceProofPath(기존경로)를 그대로 새 Resume에 복사해서 insert
        String newProofPath = saveFileAsPath(req.getATTACH_FILE_NM_M114(), "files");
        String effectiveProofPath = (newProofPath != null) ? newProofPath : safe(req.getServiceProofPath());
        if (effectiveProofPath != null) {
            ServiceProofAttachment a = new ServiceProofAttachment();
            a.setResume(saved);
            a.setAttachFileNm(effectiveProofPath);
            a.setDelYn("N");
            serviceProofAttachmentRepository.save(a);
        }

        // 3) 이력서 파일: M115
        // ✅✅✅ 수정: 새 파일이 없으면 req.resumeFilePath(기존경로)를 그대로 새 Resume에 복사해서 insert
        String newResumePath = saveFileAsPath(req.getATTACH_FILE_NM_M115(), "files");
        String effectiveResumePath = (newResumePath != null) ? newResumePath : safe(req.getResumeFilePath());
        if (effectiveResumePath != null) {
            ResumeFileAttachment a = new ResumeFileAttachment();
            a.setResume(saved);
            a.setAttachFileNm(effectiveResumePath);
            a.setDelYn("N");
            resumeFileAttachmentRepository.save(a);
        }

        // ✅ CareerHistory(M111) 저장
        if (careers != null && !careers.isEmpty()) {
            List<CareerHistory> list = new ArrayList<>();
            for (ApplyRequest.Career c : careers) {
                boolean allBlank =
                        isBlank(c.getCompany()) &&
                                isBlank(c.getDepartment()) &&
                                isBlank(c.getJoinDate()) &&
                                isBlank(c.getRetireDate()) &&
                                isBlank(c.getPosition()) &&
                                isBlank(c.getSalary()) &&
                                isBlank(c.getPositionSummary()) &&
                                isBlank(c.getExperience());
                if (allBlank) continue;

                CareerHistory ch = new CareerHistory();
                ch.setResume(saved);
                ch.setCompany(c.getCompany());
                ch.setDepartment(c.getDepartment());
                ch.setJoinDate(c.getJoinDate());
                ch.setRetireDate(c.getRetireDate());
                ch.setPosition(c.getPosition());
                ch.setSalary(c.getSalary());
                ch.setPositionSummary(c.getPositionSummary());
                ch.setExperience(c.getExperience());
                ch.setDelYn("N");
                list.add(ch);
            }
            if (!list.isEmpty()) {
                careerHistoryRepository.saveAll(list);
            }
        }

        return saved;
    }

    private ApplyRequest toApplyRequestFull(Resume r) {

        ApplyRequest dto = new ApplyRequest();

        dto.setName(r.getName());
        dto.setGender(r.getGender());
        dto.setBirthDate(r.getBirthDate());
        dto.setPhone(r.getPhone());
        dto.setEmail(r.getEmail());
        dto.setAddress(r.getAddress());

        dto.setSchool(r.getSchoolName());
        dto.setMajor(r.getMajor());
        dto.setEnrollDate(r.getEntranceDate());
        dto.setGraduateDate(r.getGradDate());
        dto.setGpa(r.getScore());
        dto.setGraduateStatus(r.getGradStatus());

        dto.setSkill(r.getSpeciality());
        dto.setSelfIntro(r.getIntroduction());

        // ✅✅✅ 파일 경로: 이제 M110에서 꺼내지 않는다

        // (1) 사진(M113) 최신 1개
        String photoPath = resumeAttachmentRepository
                .findTopByResume_SeqNoM110AndFileTypeAndDelYnOrderBySeqNoM113Desc(
                        r.getSeqNoM110(), FILETYPE_PHOTO, "N"
                )
                .map(ResumeAttachment::getFilePath)
                .orElse(null);
        dto.setPhotoPath(photoPath);

        // (2) 복무증명서(M114) 최신 1개
        String proofPath = serviceProofAttachmentRepository
                .findTopByResume_SeqNoM110AndDelYnOrderBySeqNoM114Desc(r.getSeqNoM110(), "N")
                .map(ServiceProofAttachment::getAttachFileNm)
                .orElse(null);
        dto.setServiceProofPath(proofPath);

        // (3) 이력서 파일(M115) 최신 1개
        String resumeFilePath = resumeFileAttachmentRepository
                .findTopByResume_SeqNoM110AndDelYnOrderBySeqNoM115Desc(r.getSeqNoM110(), "N")
                .map(ResumeFileAttachment::getAttachFileNm)
                .orElse(null);
        dto.setResumeFilePath(resumeFilePath);

        // ===== 경력 복원 (M111만 사용) =====
        List<ApplyRequest.Career> careers = new ArrayList<>();

        List<CareerHistory> chList =
                careerHistoryRepository.findByResume_SeqNoM110AndDelYnOrderBySeqNoM111Asc(
                        r.getSeqNoM110(), "N"
                );

        if (chList != null && !chList.isEmpty()) {
            for (CareerHistory ch : chList) {
                ApplyRequest.Career c = new ApplyRequest.Career();
                c.setCompany(ch.getCompany());
                c.setDepartment(ch.getDepartment());
                c.setJoinDate(ch.getJoinDate());
                c.setRetireDate(ch.getRetireDate());
                c.setPosition(ch.getPosition());
                c.setSalary(ch.getSalary());
                c.setPositionSummary(ch.getPositionSummary());
                c.setExperience(ch.getExperience());
                careers.add(c);
            }
        }

        dto.setCareers(careers);

        // ===== 자격증 복원 =====
        List<ApplyRequest.License> outLic = new ArrayList<>();
        if (r.getCertificates() != null) {
            for (Certificate cert : r.getCertificates()) {
                if (!"N".equalsIgnoreCase(cert.getDelYn())) continue;

                ApplyRequest.License l = new ApplyRequest.License();
                l.setCertificateNm(cert.getCertificateNm());
                l.setObtainDate(cert.getObtainDate());
                l.setAgency(cert.getAgency());
                l.setCertificateNum(cert.getCertificateNum());
                outLic.add(l);
            }
        }
        dto.setLicenses(outLic);

        return dto;
    }

    private String saveFileAsPath(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) return null;

        try {
            Path dirPath = Paths.get(uploadPath, subDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }

            String savedName = UUID.randomUUID() + "_" + System.currentTimeMillis() + ext;
            Path target = dirPath.resolve(savedName);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + subDir + "/" + savedName;

        } catch (Exception e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    private String safe(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
