package com.example.personalJobs.service;

import com.example.personalJobs.dto.ApplyRequest;
import com.example.personalJobs.entity.*;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final CareerHistoryRepository careerHistoryRepository;
    private final ServiceProofAttachmentRepository serviceProofAttachmentRepository;
    private final ResumeFileAttachmentRepository resumeFileAttachmentRepository;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    // ✅ JSON 저장/복원용
    private final ObjectMapper om = new ObjectMapper();

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
     * ✅ "입력한 모든 내용" 저장
     */
    @Transactional
    public Resume saveResumeFromApplyRequest(Integer seqNoM100, ApplyRequest req) {

        Resume r = new Resume();
        r.setSeqNoM100(seqNoM100.longValue());
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

        // ✅ 1) Resume 저장(여기서 seqNoM110 생성)
        Resume saved = resumeRepository.save(r);

        // ✅ 2) 첨부파일 저장
        saveAttachments(saved, req);

        // ✅ 3) CareerHistory(M111) 다건 저장 추가 (진짜 핵심)
        if (careers != null && !careers.isEmpty()) {

            List<CareerHistory> list = new ArrayList<>();

            for (ApplyRequest.Career c : careers) {
                // 전부 빈값이면 스킵 (안전)
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
     * ✅ "불러오면 폼에 전부 채우기"
     */
    /**
     * ✅ "불러오면 폼에 전부 채우기"
     */
    private ApplyRequest toApplyRequestFull(Resume r) {

        ApplyRequest dto = new ApplyRequest();

        // 기본
        dto.setName(r.getName());
        dto.setGender(r.getGender());
        dto.setBirthDate(r.getBirthDate());
        dto.setPhone(r.getPhone());
        dto.setEmail(r.getEmail());
        dto.setAddress(r.getAddress());

        // 최종학력
        dto.setSchool(r.getSchoolName());
        dto.setMajor(r.getMajor());
        dto.setEnrollDate(r.getEntranceDate());
        dto.setGraduateDate(r.getGradDate());
        dto.setGpa(r.getScore());
        dto.setGraduateStatus(r.getGradStatus());

        // 기술/자기소개
        dto.setSkill(r.getSpeciality());
        dto.setSelfIntro(r.getIntroduction());

        // ===== 경력 복원 (M111 우선) =====
        List<ApplyRequest.Career> careers = new ArrayList<>();

        // 1) M111에서 먼저 읽기
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
            // 2) fallback: 기존 JSON/단일 컬럼 방식
            String cd = r.getCareerDesc();

            if (cd != null && cd.trim().startsWith("[")) {
                try {
                    careers = om.readValue(
                            cd,
                            new TypeReference<List<ApplyRequest.Career>>() {}
                    );
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

        // ===== 사진 경로 =====
        dto.setPhotoPath(r.getPhotoPath());

        // ===== 복무증명서 첨부파일 =====
        List<ApplyRequest.AttachmentInfo> serviceProofFiles = new ArrayList<>();
        if (r.getServiceProofAttachments() != null) {
            for (ServiceProofAttachment spa : r.getServiceProofAttachments()) {
                if (!"N".equalsIgnoreCase(spa.getDelYn())) continue;

                ApplyRequest.AttachmentInfo info = new ApplyRequest.AttachmentInfo();
                info.setId(spa.getSeqNoM114());
                info.setFileName(spa.getAttachFileNm());
                info.setFilePath(spa.getFilePath());
                serviceProofFiles.add(info);
            }
        }
        dto.setServiceProofFiles(serviceProofFiles);

        // ===== 이력서 첨부파일 =====
        List<ApplyRequest.AttachmentInfo> resumeFiles = new ArrayList<>();
        if (r.getResumeFileAttachments() != null) {
            for (ResumeFileAttachment rfa : r.getResumeFileAttachments()) {
                if (!"N".equalsIgnoreCase(rfa.getDelYn())) continue;

                ApplyRequest.AttachmentInfo info = new ApplyRequest.AttachmentInfo();
                info.setId(rfa.getSeqNoM115());
                info.setFileName(rfa.getAttachFileNm());
                info.setFilePath(rfa.getFilePath());
                resumeFiles.add(info);
            }
        }
        dto.setResumeFiles(resumeFiles);

        return dto;
    }

    /**
     * 첨부파일 저장 (복무증명서, 이력서 파일)
     */
    private void saveAttachments(Resume resume, ApplyRequest req) {
        // 복무증명서 파일 저장 (M114)
        MultipartFile serviceProofFile = req.getATTACH_FILE_NM_M114();
        if (serviceProofFile != null && !serviceProofFile.isEmpty()) {
            try {
                String filePath = saveFile(serviceProofFile, "service-proof");
                ServiceProofAttachment attachment = new ServiceProofAttachment();
                attachment.setResume(resume);
                attachment.setAttachFileNm(serviceProofFile.getOriginalFilename());
                attachment.setFilePath(filePath);
                attachment.setDelYn("N");
                serviceProofAttachmentRepository.save(attachment);
            } catch (IOException e) {
                System.err.println("복무증명서 파일 저장 실패: " + e.getMessage());
            }
        }

        // 이력서 파일 저장 (M115)
        MultipartFile resumeFile = req.getATTACH_FILE_NM_M115();
        if (resumeFile != null && !resumeFile.isEmpty()) {
            try {
                String filePath = saveFile(resumeFile, "resume-files");
                ResumeFileAttachment attachment = new ResumeFileAttachment();
                attachment.setResume(resume);
                attachment.setAttachFileNm(resumeFile.getOriginalFilename());
                attachment.setFilePath(filePath);
                attachment.setDelYn("N");
                resumeFileAttachmentRepository.save(attachment);
            } catch (IOException e) {
                System.err.println("이력서 파일 저장 실패: " + e.getMessage());
            }
        }
    }

    /**
     * 파일 저장 (물리적 파일 저장)
     */
    private String saveFile(MultipartFile file, String subDir) throws IOException {
        Path dirPath = Paths.get(uploadPath, subDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + extension;

        Path filePath = dirPath.resolve(newFileName);
        Files.copy(file.getInputStream(), filePath);

        return "/uploads/" + subDir + "/" + newFileName;
    }
}
