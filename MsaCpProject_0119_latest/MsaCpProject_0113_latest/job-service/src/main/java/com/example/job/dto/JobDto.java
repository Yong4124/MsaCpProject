package com.example.job.dto;

import com.example.job.model.Job;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobDto {

    private Long id;
    private Integer companyId;
    private String title;
    private String jobForm;
    private String jobType;
    private String jobCategory;
    private String workLocation;
    private String workTime;
    private String industry;
    private String roleLevel;
    private String experience;
    private String baseSalary;
    private String startDate;
    private String endDate;
    private String companyName;
    private String logoPath;
    private String photoPath;

    private String companyIntro;
    private String positionSummary;
    private String skillQualification;
    private String benefits;
    private String notes;

    private String companyType;
    private String establishedDate;
    private String employeeNum;
    private String capital;
    private String revenue;
    private String homepage;
    private String ceoName;
    private String companyAddress;
    private String postingYn;
    private String closeYn;

    /**
     * Entity -> DTO 변환
     */
    public static JobDto from(Job job) {
        JobDto dto = new JobDto();

        dto.setId(job.getSeqNoM210());
        dto.setCompanyId(job.getCompanyId());
        dto.setTitle(job.getTitle());
        dto.setJobForm(job.getJobForm());
        dto.setJobType(job.getJobType());
        dto.setJobCategory(job.getJobCategory());
        dto.setWorkLocation(job.getWorkLocation());
        dto.setWorkTime(job.getWorkTime());
        dto.setIndustry(job.getIndustry());
        dto.setRoleLevel(job.getRoleLevel());
        dto.setExperience(job.getExperience());
        dto.setBaseSalary(job.getBaseSalary());
        dto.setStartDate(job.getStartDate());
        dto.setEndDate(job.getEndDate());
        dto.setPostingYn(job.getPostingYn());
        dto.setCloseYn(job.getCloseYn());

        dto.setCompanyIntro(job.getCompanyIntro());
        dto.setPositionSummary(job.getPositionSummary());
        dto.setSkillQualification(job.getSkillQualification());
        dto.setBenefits(job.getBenefits());
        dto.setNotes(job.getNotes());

        dto.setCompanyType(job.getCompanyType());
        dto.setEstablishedDate(job.getEstablishedDate());
        dto.setEmployeeNum(job.getEmployeeNum());
        dto.setCapital(job.getCapital());
        dto.setRevenue(job.getRevenue());
        dto.setHomepage(job.getHomepage());
        dto.setCeoName(job.getCeoName());
        dto.setCompanyAddress(job.getCompanyAddress());
        dto.setLogoPath(job.getLogoPath());
        dto.setPhotoPath(job.getPhotoPath());

        return dto;
    }
}
