package com.example.personalJobs.service;

import com.example.personalJobs.dto.ApplyRequest;
import com.example.personalJobs.entity.Certificate;
import com.example.personalJobs.entity.Resume;
import com.example.personalJobs.entity.CareerHistory;
import com.example.personalJobs.entity.ResumeFileAttachment;
import com.example.personalJobs.entity.ServiceProofAttachment;
import com.example.personalJobs.repository.CareerHistoryRepository;
import com.example.personalJobs.repository.ResumeFileAttachmentRepository;
import com.example.personalJobs.repository.ResumeRepository;
import com.example.personalJobs.repository.ServiceProofAttachmentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
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
    private final ServiceProofAttachmentRepository serviceProofAttachmentRepository;
    private final ResumeFileAttachmentRepository resumeFileAttachmentRepository;

    // ✅ JSON 저장/복원용
    private final ObjectMapper om = new ObjectMapper();

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

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

    /**
     * ✅ 저장
     * - 사진: uploads/photos
     * - 문서: uploads/files
     * - DB에는 "/uploads/..." 저장
     */
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

        // ===== 경력(Resume 컬럼 + JSON) =====
        List<ApplyRequest.Career> careers = (req.getCareers() != null) ? req.getCareers() : req.toCareers();

        if (careers != null && !careers.isEmpty()) {
            ApplyRequest.Career c0 = careers.get(0);
            r.setCompany(c0.getCompany());
            r.setDept(c0.getDepartment());
            r.setJoinDate(c0.getJoinDate());
            r.setLeaveDate(c0.getRetireDate());
            r.setPosition(c0.getPosition());
            r.setSalary(c0.getSalary());
            r.setTask(c0.getPositionSummary());

            try {
                String careersJson = om.writeValueAsString(careers);
                r.setCareerDesc(careersJson);
            } catch (Exception e) {
                r.setCareerDesc(c0.getExperience());
            }
        }

        // ===== 자격증 =====
        List<ApplyRequest.License> licenses = (req.getLicenses() != null) ? req.getLicenses() : req.toLicenses();
        List<Certificate> certs = new ArrayList<>();

        if (licenses != null) {
            for (ApplyRequest.License l : licenses) {
                boolean allBlank =
                        (l.getCertificateNm() == null || l.getCertificateNm().isBlank()) &&
                                (l.getObtainDate() == null || l.getObtainDate().isBlank()) &&
                                (l.getAgency() == null || l.getAgency().isBlank()) &&
                                (l.getCertificateNum() == null || l.getCertificateNum().isBlank());
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

        // =========================
        // ✅ 파일 저장
        // =========================

        // ✅✅✅ 수정 1) 사진(M113): 새 파일 없으면 req.photoPath로 유지
        String newPhotoPath = saveFileAsPath(req.getATTACH_FILE_NM_M113(), "photos");
        String effectivePhotoPath = (newPhotoPath != null) ? newPhotoPath : safe(req.getPhotoPath());
        if (effectivePhotoPath != null) {
            r.setPhotoPath(effectivePhotoPath);
        }

        // ✅✅✅ 수정 2) 이력서 파일(M115): 새 파일 없으면 req.resumeFilePath로 유지
        String newResumePath = saveFileAsPath(req.getATTACH_FILE_NM_M115(), "files");
        String effectiveResumePath = (newResumePath != null) ? newResumePath : safe(req.getResumeFilePath());
        if (effectiveResumePath != null) {
            r.setFilePath(effectiveResumePath);
        }

        // ✅ Resume 저장
        Resume saved = resumeRepository.save(r);

        // ✅✅✅ 수정 3) 복무증명서(M114): 새 파일 없으면 req.serviceProofPath로 유지
        String newProofPath = saveFileAsPath(req.getATTACH_FILE_NM_M114(), "files");
        String effectiveProofPath = (newProofPath != null) ? newProofPath : safe(req.getServiceProofPath());
        if (effectiveProofPath != null) {
            ServiceProofAttachment a = new ServiceProofAttachment();
            a.setResume(saved);
            a.setAttachFileNm(effectiveProofPath);
            a.setDelYn("N");
            serviceProofAttachmentRepository.save(a);
        }

        // ✅ 이력서 첨부(M115) - 새 파일이든 기존 파일이든, 새 Resume 버전에 한 줄 남김
        if (effectiveResumePath != null) {
            ResumeFileAttachment a = new ResumeFileAttachment();
            a.setResume(saved);
            a.setAttachFileNm(effectiveResumePath);
            a.setDelYn("N");
            resumeFileAttachmentRepository.save(a);
        }

        // ✅ CareerHistory(M111) 다건 저장
        if (careers != null && !careers.isEmpty()) {
            List<CareerHistory> list = new ArrayList<>();

            for (ApplyRequest.Career c : careers) {
                boolean allBlank =
                        (c.getCompany() == null || c.getCompany().isBlank()) &&
                                (c.getDepartment() == null || c.getDepartment().isBlank()) &&
                                (c.getJoinDate() == null || c.getJoinDate().isBlank()) &&
                                (c.getRetireDate() == null || c.getRetireDate().isBlank()) &&
                                (c.getPosition() == null || c.getPosition().isBlank()) &&
                                (c.getSalary() == null || c.getSalary().isBlank()) &&
                                (c.getPositionSummary() == null || c.getPositionSummary().isBlank()) &&
                                (c.getExperience() == null || c.getExperience().isBlank());
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

    /**
     * ✅ 불러오기 DTO 만들기 (여기에 파일 경로 3개 내려줌)
     */
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

        // ✅ 파일 경로 내려주기
        dto.setPhotoPath(r.getPhotoPath());     // M113 (Resume 컬럼)
        dto.setResumeFilePath(r.getFilePath()); // M115 (Resume 컬럼)

        // M114는 첨부테이블에서 최신 1개
        String proofPath = serviceProofAttachmentRepository
                .findTopByResume_SeqNoM110AndDelYnOrderBySeqNoM114Desc(r.getSeqNoM110(), "N")
                .map(ServiceProofAttachment::getAttachFileNm)
                .orElse(null);
        dto.setServiceProofPath(proofPath);

        // ===== 경력 복원 (M111 우선) =====
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
        } else {
            String cd = r.getCareerDesc();

            if (cd != null && cd.trim().startsWith("[")) {
                try {
                    careers = om.readValue(cd, new TypeReference<List<ApplyRequest.Career>>() {});
                } catch (Exception ignore) {
                    careers = new ArrayList<>();
                }
            }

            if (careers == null || careers.isEmpty()) {
                ApplyRequest.Career c = new ApplyRequest.Career();
                c.setCompany(r.getCompany());
                c.setDepartment(r.getDept());
                c.setJoinDate(r.getJoinDate());
                c.setRetireDate(r.getLeaveDate());
                c.setPosition(r.getPosition());
                c.setSalary(r.getSalary());
                c.setPositionSummary(r.getTask());
                c.setExperience(r.getCareerDesc());
                careers = List.of(c);
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

    // ✅✅✅ 추가: "" 같은 값은 null 처리해서 덮어쓰기 방지
    private String safe(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
