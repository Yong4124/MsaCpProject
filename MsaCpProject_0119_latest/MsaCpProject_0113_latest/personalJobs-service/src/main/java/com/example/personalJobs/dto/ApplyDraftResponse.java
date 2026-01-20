package com.example.personalJobs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApplyDraftResponse {
    private boolean exists;          // 초안 존재 여부
    private String status;           // NONE / TEMP / SUBMITTED
    private Long applicationId;      // seq_no_m300
    private ApplyRequest data;       // 저장된 스냅샷(JSON) -> ApplyRequest
}
