package com.example.personalJobs.service;

import com.example.personalJobs.dto.ApplyRequest;
import com.example.personalJobs.entity.Certificate;
import com.example.personalJobs.entity.Resume;
import com.example.personalJobs.repository.ResumeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;

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

        // ===== 경력 =====
        // 1) 화면 입력을 리스트로 확보 (toCareers()가 있으면 그걸 쓰고 없으면 careers 그대로)
        List<ApplyRequest.Career> careers = (req.getCareers() != null) ? req.getCareers() : req.toCareers();

        if (careers != null && !careers.isEmpty()) {
            // 테이블 구조상 단일 컬럼 세트가 있으니 "첫번째 경력"은 컬럼에 채워둠
            ApplyRequest.Career c0 = careers.get(0);
            r.setCompany(c0.getCompany());
            r.setDept(c0.getDepartment());
            r.setJoinDate(c0.getJoinDate());
            r.setLeaveDate(c0.getRetireDate());
            r.setPosition(c0.getPosition());
            r.setSalary(c0.getSalary());
            r.setTask(c0.getPositionSummary());

            // 기존엔 careerDesc에 experience만 넣었는데,
            // 이제 "전체 경력 리스트"를 JSON으로 저장해서 나중에 그대로 복원함
            try {
                String careersJson = om.writeValueAsString(careers);
                r.setCareerDesc(careersJson);
            } catch (Exception e) {
                // JSON 실패하면 최소한 첫번째 경험이라도 저장
                r.setCareerDesc(c0.getExperience());
            }
        }

        // ===== 자격증 =====
        // Certificate 엔티티로 전부 저장
        List<ApplyRequest.License> licenses = (req.getLicenses() != null) ? req.getLicenses() : req.toLicenses();
        List<Certificate> certs = new ArrayList<>();

        if (licenses != null) {
            for (ApplyRequest.License l : licenses) {
                // 전부 빈값이면 스킵
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

        return resumeRepository.save(r);
    }

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

        // ===== 경력 복원 =====
        // careerDesc가 JSON이면 경력 리스트 전체 복원
        List<ApplyRequest.Career> careers = new ArrayList<>();
        String cd = r.getCareerDesc();

        if (cd != null && cd.trim().startsWith("[")) {
            try {
                careers = om.readValue(cd, new TypeReference<List<ApplyRequest.Career>>() {});
            } catch (Exception ignore) {
                careers = new ArrayList<>();
            }
        }

        // JSON 복원이 실패하거나 비어있으면 "단일 컬럼"로라도 1개 만들어 줌
        if (careers == null || careers.isEmpty()) {
            ApplyRequest.Career c = new ApplyRequest.Career();
            c.setCompany(r.getCompany());
            c.setDepartment(r.getDept());
            c.setJoinDate(r.getJoinDate());
            c.setRetireDate(r.getLeaveDate());
            c.setPosition(r.getPosition());
            c.setSalary(r.getSalary());
            c.setPositionSummary(r.getTask());
            c.setExperience(r.getCareerDesc()); // JSON 아니면 그냥 텍스트일 수도 있으니까
            careers = List.of(c);
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
}
