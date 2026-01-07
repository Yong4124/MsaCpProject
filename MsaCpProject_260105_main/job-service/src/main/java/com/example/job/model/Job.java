package com.example.job.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "T_JB_M210")
@Getter
@Setter
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEQ_NO_M210")
    private Long seqNoM210;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEQ_NO_M200", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private CompanyMember company;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "START_DATE")
    private String startDate;

    @Column(name = "END_DATE")
    private String endDate;

    @Column(name = "JOB_FORM")
    private String jobForm;

    @Column(name = "JOB_TYPE")
    private String jobType;

    @Column(name = "JOB_CATEGORY")
    private String jobCategory;

    @Column(name = "INDUSTRY")
    private String industry;

    @Column(name = "WORK_TIME")
    private String workTime;

    @Column(name = "ROLE_LEVEL")
    private String roleLevel;

    @Column(name = "BASE_SALARY")
    private String baseSalary;

    @Column(name = "EXPERIENCE")
    private String experience;

    @Column(name = "JOB_LOCATION")
    private String workLocation;

    @Lob
    @Column(name = "COMPANY_INTRO", columnDefinition = "TEXT")
    private String companyIntro;

    @Lob
    @Column(name = "POSITION_SUMMARY", columnDefinition = "TEXT")
    private String positionSummary;

    @Lob
    @Column(name = "SKILL_QUALIFICATION", columnDefinition = "TEXT")
    private String skillQualification;

    @Lob
    @Column(name = "BENEFITS", columnDefinition = "TEXT")
    private String benefits;

    @Lob
    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "COMPANY_TYPE")
    private String companyType;

    @Column(name = "ESTABLISHED_DATE")
    private String establishedDate;

    @Column(name = "EMPLOYEE_NUM")
    private String employeeNum;

    @Column(name = "CAPITAL")
    private String capital;

    @Column(name = "REVENUE")
    private String revenue;

    @Column(name = "HOMEPAGE")
    private String homepage;

    @Column(name = "POSTING_YN")
    private String postingYn = "1";

    @Column(name = "CLOSE_YN")
    private String closeYn = "N";

    @Column(name = "DEL_YN")
    private String delYn = "N";
}