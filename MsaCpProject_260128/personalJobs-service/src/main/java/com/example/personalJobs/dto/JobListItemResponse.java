package com.example.personalJobs.dto;

public record JobListItemResponse(
        Long id,
        String title,
        String company,
        String workType,
        String employmentType,
        String jobCategory,
        String industry,
        String level,
        String experience,
        String salaryText,
        String workingHours,
        String location,
        String logoUrl,
        boolean marked,
        boolean closed
) {}
