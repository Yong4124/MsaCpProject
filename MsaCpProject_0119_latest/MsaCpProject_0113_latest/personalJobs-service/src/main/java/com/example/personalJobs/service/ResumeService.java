package com.example.personalJobs.service;

import com.example.personalJobs.dto.ApplyRequest;
import com.example.personalJobs.entity.Certificate;
import com.example.personalJobs.entity.Resume;
import com.example.personalJobs.repository.ResumeRepository;
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

    @Transactional(readOnly = true)
    public Optional<ApplyRequest> getLatestResumeAsApplyRequest(Integer seqNoM100) {

        Long m100 = seqNoM100.longValue();

        Optional<Resume> opt =
                resumeRepository.findTopBySeqNoM100AndDelYnOrderBySeqNoM110Desc(m100, "N");

        if (opt.isEmpty()) return Optional.empty();

        Resume r = opt.get();
        ApplyRequest dto = new ApplyRequest();

        dto.setPhone(r.getPhone());
        dto.setEmail(r.getEmail());
        dto.setAddress(r.getAddress());
        dto.setSchool(r.getSchoolName());
        dto.setMajor(r.getMajor());
        dto.setSelfIntro(r.getIntroduction());

        // 경력 (ApplyRequest 구조에 맞춰 1개만 담아도 됨)
        ApplyRequest.Career c = new ApplyRequest.Career();
        c.setCompany(r.getCompany());
        c.setDepartment(r.getDept());            // ✅ dept
        c.setJoinDate(r.getJoinDate());
        c.setRetireDate(r.getLeaveDate());       // ✅ leaveDate
        c.setPosition(r.getPosition());

        // experience 자리에 careerDesc 우선, 없으면 task
        String exp = (r.getCareerDesc() != null && !r.getCareerDesc().isBlank())
                ? r.getCareerDesc()
                : r.getTask();
        c.setExperience(exp);

        dto.setCareers(java.util.List.of(c));

        // 자격증
        java.util.List<ApplyRequest.License> licenses = new java.util.ArrayList<>();
        if (r.getCertificates() != null) {
            for (Certificate cert : r.getCertificates()) {
                if (!"N".equalsIgnoreCase(cert.getDelYn())) continue;
                ApplyRequest.License l = new ApplyRequest.License();
                l.setCertificateNm(cert.getCertificateNm());
                l.setObtainDate(cert.getObtainDate());
                l.setAgency(cert.getAgency());
                l.setCertificateNum(cert.getCertificateNum());
                licenses.add(l);
            }
        }
        dto.setLicenses(licenses);

        return Optional.of(dto);
    }
    @Transactional(readOnly = true)
    public Optional<ApplyRequest> getResumeAsApplyRequestByM110(Long seqNoM110) {
        return resumeRepository.findById(seqNoM110)
                .filter(r -> "N".equalsIgnoreCase(r.getDelYn()))
                .map(this::toApplyRequest);
    }

    @Transactional
    public Resume saveResumeFromApplyRequest(Integer seqNoM100, ApplyRequest req) {
        Resume r = new Resume();
        r.setSeqNoM100(seqNoM100.longValue());
        r.setDelYn("N");

        // ✅ 기본
        r.setPhone(req.getPhone());
        r.setEmail(req.getEmail());
        r.setAddress(req.getAddress());
        r.setSchoolName(req.getSchool());
        r.setMajor(req.getMajor());
        r.setIntroduction(req.getSelfIntro());

        // ✅ 경력(네 Resume 엔티티 필드 기준)
        if (req.getCareers() != null && !req.getCareers().isEmpty()) {
            ApplyRequest.Career c = req.getCareers().get(0);
            r.setCompany(c.getCompany());
            r.setDept(c.getDepartment());       // dept
            r.setJoinDate(c.getJoinDate());
            r.setLeaveDate(c.getRetireDate());  // leaveDate
            r.setPosition(c.getPosition());

            // experience -> careerDesc or task
            r.setCareerDesc(c.getExperience());
        }

        // ⚠️ 자격증/첨부는 지금 단계에선 “나중에” 붙여도 됨
        // (이미 Certificate 엔티티 올려줬으니 다음 단계에서 연결해주면 됨)

        return resumeRepository.save(r);
    }

    private ApplyRequest toApplyRequest(Resume r) {
        ApplyRequest dto = new ApplyRequest();
        dto.setPhone(r.getPhone());
        dto.setEmail(r.getEmail());
        dto.setAddress(r.getAddress());
        dto.setSchool(r.getSchoolName());
        dto.setMajor(r.getMajor());
        dto.setSelfIntro(r.getIntroduction());

        ApplyRequest.Career c = new ApplyRequest.Career();
        c.setCompany(r.getCompany());
        c.setDepartment(r.getDept());
        c.setJoinDate(r.getJoinDate());
        c.setRetireDate(r.getLeaveDate());
        c.setPosition(r.getPosition());

        String exp = (r.getCareerDesc() != null && !r.getCareerDesc().isBlank())
                ? r.getCareerDesc()
                : r.getTask();
        c.setExperience(exp);

        dto.setCareers(java.util.List.of(c));

        // 자격증은 다음 단계에서 Certificate 연결해서 채우면 됨
        dto.setLicenses(java.util.List.of());

        return dto;
    }

}
