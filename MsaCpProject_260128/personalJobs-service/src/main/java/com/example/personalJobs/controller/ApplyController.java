package com.example.personalJobs.controller;

import com.example.personalJobs.dto.*;
import com.example.personalJobs.service.ApplyService;
import com.example.personalJobs.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/apply")
public class ApplyController {

    private final AuthService authService;
    private final ApplyService applyService;

    // ✅ 임시저장 내역 불러오기
    @GetMapping("/{jobId}/draft")
    public ApiResponse<ApplyDraftResponse> draft(
            @PathVariable Long jobId,
            @CookieValue(value = "JWT_TOKEN", required = false) String token,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request
    ) {
        Integer seqNoM100 = authService.requireSeqNoM100(token, authorization, request);
        // Service가 알아서 Application 테이블 없으면 최신 이력서를 가져다 줌
        return ApiResponse.ok(applyService.getDraft(jobId, seqNoM100));
    }

    // ✅ 임시저장 (수정됨: Service에서 Application 테이블 저장 안 함)
    @PostMapping(
            value = "/temp",
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE }
    )
    public ApiResponse<ApplySaveResponse> tempSave(
            @CookieValue(value = "JWT_TOKEN", required = false) String token,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request,
            @ModelAttribute ApplyRequest req
    ) {
        Integer seqNoM100 = authService.requireSeqNoM100(token, authorization, request);

        // DTO에서 ID 꺼내기 (DTO에 getter가 있어야 함. getSEQ_NO_M210() 또는 getSeqNoM210())
        Long jobId = req.getSEQ_NO_M210();

        return ApiResponse.ok(applyService.tempSave(jobId, seqNoM100, req));
    }

    // ✅ 최종 제출
    @PostMapping(
            value = "/submit",
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE }
    )
    public ResponseEntity<ApiResponse<ApplySaveResponse>> submit(
            @CookieValue(value = "JWT_TOKEN", required = false) String token,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request,
            @ModelAttribute ApplyRequest req
    ) {
        try {
            Integer seqNoM100 = authService.requireSeqNoM100(token, authorization, request);
            Long jobId = req.getSEQ_NO_M210();

            ApplySaveResponse response = applyService.submit(jobId, seqNoM100, req);
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (IllegalStateException e) {
            // 중복 지원 에러 처리 (이미 제출한 경우)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // 서버 로그에 에러 원인 출력
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("제출 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/my")
    public ApiResponse<MyApplyListResponse> my(
            @CookieValue(value = "JWT_TOKEN", required = false) String token,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Integer seqNoM100 = authService.requireSeqNoM100(token, authorization, request);
        return ApiResponse.ok(applyService.getMyApplyList(seqNoM100, page, size));
    }
}