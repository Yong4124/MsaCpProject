package com.example.personalJobs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyApplyItemDto {
    private Long seqNoM300;

    private Long jobId;        // t_jb_m300.job_id
    private Long resumeId;     // t_jb_m300.seq_no_m110

    private String statusText; // "임시저장" / "제출완료" / "지원취소"
    private String reviewStatus;
    private String cancelStatus;

    private String companyName; // 공고 상세 API에서
    private String title;       // 공고 상세 API에서
    private String logoPath;    // 공고 상세 API에서
}
