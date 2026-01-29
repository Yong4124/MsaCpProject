package com.example.personalJobs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MyApplyListResponse {
    private List<MyApplyItemDto> items;
    private int page;
    private int totalPages;
    private long totalElements;
}
