package com.example.personalJobs.controller;

import com.example.personalJobs.dto.JobListItemResponse;
import com.example.personalJobs.dto.SearchField;
import com.example.personalJobs.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    // 예: GET /api/jobs?markoption=1&searchfield=TITLE&searchword=Engineer&sortoption=START_DATE&page=0&size=10
    @GetMapping
    public Page<JobListItemResponse> list(
            @RequestParam(name = "markoption", required = false) Integer markOption,
            @RequestParam(name = "group", required = false) String groupCompany,
            @RequestParam(name = "searchfield", required = false) SearchField searchField,
            @RequestParam(name = "searchword", required = false) String searchWord,
            @RequestParam(name = "sortoption", defaultValue = "START_DATE") String sortOption,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return jobService.getJobs(markOption, groupCompany, searchField, searchWord, sortOption, page, size);
    }

    @GetMapping("/{id}")
    public JobListItemResponse detail(@PathVariable Long id) {
        // 디테일도 서비스로 분리하고 싶으면 JobService에 getJob(id) 추가하면 됨
        // 지금은 기존 JobService 설계상 list 중심이라 detail은 별도 구현이 필요.
        // -> 빠르게: JobService에 getJobDetail 추가하는 방식 추천
        return jobService.getJobDetail(id);
    }
}
