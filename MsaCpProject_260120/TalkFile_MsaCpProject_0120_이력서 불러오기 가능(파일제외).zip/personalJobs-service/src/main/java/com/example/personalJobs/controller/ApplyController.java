package com.example.personalJobs.controller;

import com.example.personalJobs.dto.*;
import com.example.personalJobs.service.ApplyService;
import com.example.personalJobs.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/apply")
public class ApplyController {

    private final AuthService authService;
    private final ApplyService applyService;

    @GetMapping("/{jobId}/draft")
    public ApiResponse<ApplyDraftResponse> draft(
            @PathVariable Long jobId,
            @CookieValue(value = "JWT_TOKEN", required = false) String token,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request
    ) {
        Integer seqNoM100 = authService.requireSeqNoM100(token, authorization, request);
        return ApiResponse.ok(applyService.getDraft(jobId, seqNoM100));
    }

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

        Long jobId = req.getSEQ_NO_M210();
        return ApiResponse.ok(applyService.tempSave(jobId, seqNoM100, req));
    }

    @PostMapping(
            value = "/submit",
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE }
    )
    public ApiResponse<ApplySaveResponse> submit(
            @CookieValue(value = "JWT_TOKEN", required = false) String token,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request,
            @ModelAttribute ApplyRequest req
    ) {
        Integer seqNoM100 = authService.requireSeqNoM100(token, authorization, request);

        Long jobId = req.getSEQ_NO_M210();
        return ApiResponse.ok(applyService.submit(jobId, seqNoM100, req));
    }
}
