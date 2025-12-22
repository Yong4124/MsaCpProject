package com.example.demo.controller;

import com.example.demo.entity.JobPosting;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.FavoriteJobService;
import com.example.demo.service.JobPostingService;
import com.example.demo.service.PersonalMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/job")
@RequiredArgsConstructor
public class JobController {

    private final JobPostingService jobPostingService;
    private final PersonalMemberService personalMemberService;
    private final FavoriteJobService favoriteJobService;
    private final ApplicationService applicationService;

    // 채용공고 목록
    @GetMapping("/list")
    public String list(@RequestParam(required = false) String keyword, Model model) {
        List<JobPosting> jobPostings;

        if (keyword != null && !keyword.trim().isEmpty()) {
            jobPostings = jobPostingService.search(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            jobPostings = jobPostingService.findActiveJobPostings();
        }

        model.addAttribute("jobPostings", jobPostings);
        return "job/list";
    }

    // 채용공고 상세
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        JobPosting jobPosting = jobPostingService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채용공고입니다."));

        model.addAttribute("job", jobPosting);

        // D-Day 계산
        long dDay = 0;
        if (jobPosting.getEndDate() != null) {
            dDay = ChronoUnit.DAYS.between(LocalDate.now(), jobPosting.getEndDate());
        }
        model.addAttribute("dDay", dDay);

        // 로그인한 개인회원인 경우 관심등록 여부, 지원 여부 확인
        if (authentication != null) {
            String loginId = authentication.getName();
            personalMemberService.findByLoginId(loginId).ifPresent(member -> {
                boolean isFavorite = favoriteJobService.isFavorite(member.getId(), id);
                boolean hasApplied = applicationService.existsByPersonalMemberIdAndJobPostingId(member.getId(), id);
                model.addAttribute("isFavorite", isFavorite);
                model.addAttribute("hasApplied", hasApplied);
                model.addAttribute("memberId", member.getId());
            });
        }

        return "job/detail";
    }
}