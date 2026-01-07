package com.example.job.service;

import com.example.job.dto.JobDto;
import com.example.job.model.Job;
import com.example.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobService {

    private final JobRepository jobRepository;

    public List<JobDto> getJobList(Long companyId) {
        return jobRepository.findByDelYnOrderBySeqNoM210Desc("N")
                .stream()
                .map(JobDto::from)
                .toList();
    }

    public JobDto getJobDtoById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다. ID: " + id));
        return JobDto.from(job);
    }

    @Transactional
    public JobDto create(Job job) {
        job.setDelYn("N");

        if (job.getPostingYn() == null || job.getPostingYn().isEmpty()) {
            job.setPostingYn("1");
        }

        if (job.getCloseYn() == null || job.getCloseYn().isEmpty()) {
            job.setCloseYn("N");
        }

        Job saved = jobRepository.save(job);
        return JobDto.from(saved);
    }

    @Transactional
    public JobDto update(Long id, Job req) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다. ID: " + id));

        job.setTitle(req.getTitle());
        job.setStartDate(req.getStartDate());
        job.setEndDate(req.getEndDate());
        job.setJobForm(req.getJobForm());
        job.setJobType(req.getJobType());
        job.setJobCategory(req.getJobCategory());
        job.setIndustry(req.getIndustry());
        job.setRoleLevel(req.getRoleLevel());
        job.setExperience(req.getExperience());
        job.setBaseSalary(req.getBaseSalary());
        job.setWorkTime(req.getWorkTime());
        job.setWorkLocation(req.getWorkLocation());

        job.setCompanyIntro(req.getCompanyIntro());
        job.setPositionSummary(req.getPositionSummary());
        job.setSkillQualification(req.getSkillQualification());
        job.setBenefits(req.getBenefits());
        job.setNotes(req.getNotes());

        job.setCompanyType(req.getCompanyType());
        job.setEstablishedDate(req.getEstablishedDate());
        job.setEmployeeNum(req.getEmployeeNum());
        job.setCapital(req.getCapital());
        job.setRevenue(req.getRevenue());
        job.setHomepage(req.getHomepage());

        if (req.getPostingYn() != null) {
            job.setPostingYn(req.getPostingYn());
        }

        return JobDto.from(job);
    }

    @Transactional
    public void close(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다. ID: " + id));
        job.setCloseYn("Y");
    }

    @Transactional
    public void delete(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다. ID: " + id));
        job.setDelYn("Y");
    }
}