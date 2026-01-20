package com.example.personalJobs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApplySaveResponse {
    private boolean success;
    private String status;       // TEMP / SUBMITTED
    private Long applicationId;  // seq_no_m300
}
