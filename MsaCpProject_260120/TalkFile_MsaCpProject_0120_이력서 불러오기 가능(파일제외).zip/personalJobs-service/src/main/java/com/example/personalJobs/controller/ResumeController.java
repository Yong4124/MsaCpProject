package com.example.personalJobs.controller;

import com.example.personalJobs.dto.ApiResponse;
import com.example.personalJobs.dto.ApplyRequest;
import com.example.personalJobs.entity.Resume;
import com.example.personalJobs.repository.ResumeRepository;
import com.example.personalJobs.service.AuthService;
import com.example.personalJobs.service.ResumeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resume")
public class ResumeController {

    private final AuthService authService;
    private final ResumeService resumeService;
    private final ResumeRepository resumeRepository;

    // ✅ 기존: 최신 1개 불러오기
    @GetMapping("/me")
    public ApiResponse<ApplyRequest> myResume(HttpServletRequest request) {
        Integer m100 = authService.requireSeqNoM100(request);
        return resumeService.getLatestResumeAsApplyRequest(m100)
                .map(ApiResponse::ok)
                .orElseGet(() -> ApiResponse.fail("불러올 이력서가 없습니다."));
    }

    // ✅ 추가: 내 이력서 목록 (팝업 리스트용)
    @GetMapping("/list")
    public ApiResponse<ResumeListResponse> list(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Integer m100i = authService.requireSeqNoM100(request);
        Long m100 = m100i.longValue();

        Page<Resume> p = resumeRepository.findBySeqNoM100AndDelYnOrderBySeqNoM110Desc(
                m100, "N", PageRequest.of(Math.max(page, 0), Math.min(size, 20))
        );

        List<ResumeListItem> items = p.getContent().stream()
                .map(r -> new ResumeListItem(
                        r.getSeqNoM110(),
                        r.getInsertDate(),
                        r.getSchoolName(),
                        r.getMajor()
                ))
                .toList();

        return ApiResponse.ok(new ResumeListResponse(
                items,
                p.getNumber(),
                p.getTotalPages(),
                p.getTotalElements()
        ));
    }

    // ✅ 추가: 선택한 이력서 1개 불러오기 (m110 기준)
    @GetMapping("/{seqNoM110}")
    public ApiResponse<ApplyRequest> one(
            HttpServletRequest request,
            @PathVariable Long seqNoM110
    ) {
        Integer m100i = authService.requireSeqNoM100(request);
        Long m100 = m100i.longValue();

        // ✅ 내 이력서만 열리게 보안 체크
        return resumeRepository.findBySeqNoM110AndSeqNoM100AndDelYn(seqNoM110, m100, "N")
                .flatMap(r -> resumeService.getResumeAsApplyRequestByM110(r.getSeqNoM110()))
                .map(ApiResponse::ok)
                .orElseGet(() -> ApiResponse.fail("이력서를 불러올 수 없습니다."));
    }

    // ===== DTO (컨트롤러 안에 넣어도 됨) =====
    @Getter
    public static class ResumeListResponse {
        private final List<ResumeListItem> items;
        private final int page;
        private final int totalPages;
        private final long totalElements;

        public ResumeListResponse(List<ResumeListItem> items, int page, int totalPages, long totalElements) {
            this.items = items;
            this.page = page;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
        }
    }

    @Getter
    public static class ResumeListItem {
        private final Long seqNoM110;
        private final String insertDate;
        private final String school;
        private final String major;

        public ResumeListItem(Long seqNoM110, String insertDate, String school, String major) {
            this.seqNoM110 = seqNoM110;
            this.insertDate = insertDate;
            this.school = school;
            this.major = major;
        }
    }
}
