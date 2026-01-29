package com.example.personalJobs.controller;

import com.example.personalJobs.dto.JobListItemResponse;
import com.example.personalJobs.dto.SearchField;
import com.example.personalJobs.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/jobs")
public class JobPublicController {

    private final JobService jobService;

    // ✅ 프론트가 기대하는: "배열(List)" 반환
    @GetMapping
    public List<JobListItemResponse> list(
            @RequestParam(name = "markoption", required = false) Integer markOption,
            @RequestParam(name = "group", required = false) String groupCompany,
            @RequestParam(name = "searchfield", required = false) SearchField searchField,
            @RequestParam(name = "searchword", required = false) String searchWord,
            @RequestParam(name = "sortoption", defaultValue = "START_DATE") String sortOption
    ) {
        // size 크게 해서 전체 받아서 content만 반환 (JS가 배열 기대하니까)
        Page<JobListItemResponse> page = jobService.getJobs(
                markOption, groupCompany, searchField, searchWord, sortOption, 0, 5000
        );
        return page.getContent();
    }

    @GetMapping("/{id}")
    public JobListItemResponse detail(@PathVariable Long id) {
        return jobService.getJobDetail(id);
    }
}
