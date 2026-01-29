package com.example.personalJobs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyApplyItemDto {
    private Long seqNoM300;
    private Long jobId;
    private Long resumeId;

    private String statusText;
    private String reviewStatus;
    private String cancelStatus;

    // 공고 카드 기본
    private String companyName;
    private String title;
    private String logoPath;

    // 👇 추가
    private String workType;
    private String employmentType;
    private String industry;
    private String level;
    private String experience;
    private String salaryText;
    private String workingHours;
    private String location;

    private boolean closed;
    private String ddayText; // "D-11", "마감", "채용완료"
}
