package com.example.job.service;

import com.example.job.dto.JobDto;
import com.example.job.model.Job;
import com.example.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobService {

    private final JobRepository jobRepository;

    /**
     * 특정 회사의 채용공고 목록 조회
     */
    public List<JobDto> getJobList(Long companyId) {
        return jobRepository
                .findByCompanyIdAndDelYnOrderBySeqNoM210Desc(companyId.intValue(), "N")
                .stream()
                .map(JobDto::from)
                .toList();
    }

    /**
     * 채용공고 상세 조회
     */
    public JobDto getJobDtoById(Long id) {
        Job job = jobRepository.findBySeqNoM210AndDelYn(id, "N")
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다. ID: " + id));
        return JobDto.from(job);
    }

    /**
     * 채용공고 등록 (회사 정보 포함)
     */
    @Transactional
    public JobDto createWithCompany(Map<String, Object> payload, Long companyId) {
        Job job = new Job();

        // 회사 ID 설정
        job.setCompanyId(companyId.intValue());

        // 기본 정보
        job.setTitle(getStringValue(payload, "title"));
        job.setStartDate(getStringValue(payload, "startDate"));
        job.setEndDate(getStringValue(payload, "endDate"));
        job.setJobForm(getStringValue(payload, "jobForm"));
        job.setJobType(getStringValue(payload, "jobType"));
        job.setJobCategory(getStringValue(payload, "jobCategory"));
        job.setWorkLocation(getStringValue(payload, "workLocation"));
        job.setWorkTime(getStringValue(payload, "workTime"));
        job.setIndustry(getStringValue(payload, "industry"));
        job.setRoleLevel(getStringValue(payload, "roleLevel"));
        job.setExperience(getStringValue(payload, "experience"));
        job.setBaseSalary(getStringValue(payload, "baseSalary"));

        // 상세 정보
        job.setCompanyIntro(getStringValue(payload, "companyIntro"));
        job.setPositionSummary(getStringValue(payload, "positionSummary"));
        job.setSkillQualification(getStringValue(payload, "skillQualification"));
        job.setBenefits(getStringValue(payload, "benefits"));
        job.setNotes(getStringValue(payload, "notes"));

        // 회사 정보
        job.setCompanyType(getStringValue(payload, "companyType"));
        job.setEstablishedDate(getStringValue(payload, "establishedDate"));
        job.setEmployeeNum(getStringValue(payload, "employeeNum"));
        job.setCapital(getStringValue(payload, "capital"));
        job.setRevenue(getStringValue(payload, "revenue"));
        job.setHomepage(getStringValue(payload, "homepage"));
        job.setCeoName(getStringValue(payload, "ceoName"));
        job.setCompanyAddress(getStringValue(payload, "companyAddress"));
        job.setLogoPath(getStringValue(payload, "logoPath"));
        job.setPhotoPath(getStringValue(payload, "photoPath"));

        // 상태 정보
        String postingYn = getStringValue(payload, "postingYn");
        job.setPostingYn(postingYn != null && !postingYn.isEmpty() ? postingYn : "1");
        job.setCloseYn("N");
        job.setDelYn("N");

        Job saved = jobRepository.save(job);
        return JobDto.from(saved);
    }

    /**
     * 채용공고 수정
     */
    @Transactional
    public JobDto update(Long id, Job req) {
        Job job = jobRepository.findBySeqNoM210AndDelYn(id, "N")
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다. ID: " + id));

        // 기본 정보 수정
        if (req.getTitle() != null) job.setTitle(req.getTitle());
        if (req.getStartDate() != null) job.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) job.setEndDate(req.getEndDate());
        if (req.getJobForm() != null) job.setJobForm(req.getJobForm());
        if (req.getJobType() != null) job.setJobType(req.getJobType());
        if (req.getJobCategory() != null) job.setJobCategory(req.getJobCategory());
        if (req.getIndustry() != null) job.setIndustry(req.getIndustry());
        if (req.getRoleLevel() != null) job.setRoleLevel(req.getRoleLevel());
        if (req.getExperience() != null) job.setExperience(req.getExperience());
        if (req.getBaseSalary() != null) job.setBaseSalary(req.getBaseSalary());
        if (req.getWorkTime() != null) job.setWorkTime(req.getWorkTime());
        if (req.getWorkLocation() != null) job.setWorkLocation(req.getWorkLocation());

        // 상세 정보 수정
        if (req.getCompanyIntro() != null) job.setCompanyIntro(req.getCompanyIntro());
        if (req.getPositionSummary() != null) job.setPositionSummary(req.getPositionSummary());
        if (req.getSkillQualification() != null) job.setSkillQualification(req.getSkillQualification());
        if (req.getBenefits() != null) job.setBenefits(req.getBenefits());
        if (req.getNotes() != null) job.setNotes(req.getNotes());

        // 회사 정보 수정
        if (req.getCompanyType() != null) job.setCompanyType(req.getCompanyType());
        if (req.getEstablishedDate() != null) job.setEstablishedDate(req.getEstablishedDate());
        if (req.getEmployeeNum() != null) job.setEmployeeNum(req.getEmployeeNum());
        if (req.getCapital() != null) job.setCapital(req.getCapital());
        if (req.getRevenue() != null) job.setRevenue(req.getRevenue());
        if (req.getHomepage() != null) job.setHomepage(req.getHomepage());
        if (req.getCeoName() != null) job.setCeoName(req.getCeoName());
        if (req.getCompanyAddress() != null) job.setCompanyAddress(req.getCompanyAddress());
        if (req.getLogoPath() != null) job.setLogoPath(req.getLogoPath());
        if (req.getPhotoPath() != null) job.setPhotoPath(req.getPhotoPath());

        // 게시 상태 수정
        if (req.getPostingYn() != null) job.setPostingYn(req.getPostingYn());

        return JobDto.from(job);
    }

    /**
     * 채용공고 마감
     */
    @Transactional
    public void close(Long id) {
        Job job = jobRepository.findBySeqNoM210AndDelYn(id, "N")
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다. ID: " + id));
        job.setCloseYn("Y");
    }

    /**
     * 채용공고 삭제 (논리 삭제)
     */
    @Transactional
    public void delete(Long id) {
        Job job = jobRepository.findBySeqNoM210AndDelYn(id, "N")
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다. ID: " + id));
        job.setDelYn("Y");
    }

    /**
     * Map에서 String 값을 안전하게 추출하는 헬퍼 메서드
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}