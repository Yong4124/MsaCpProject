package com.example.personalJobs.controller;

import com.example.personalJobs.dto.*;
import com.example.personalJobs.service.ApplyService;
import com.example.personalJobs.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    // ✅ multipart/form-data, application/x-www-form-urlencoded 둘 다 받게 열기
    @PostMapping(
            value = "/temp",
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE }
    )
    public ApiResponse<ApplySaveResponse> tempSave(
            @CookieValue(value = "JWT_TOKEN", required = false) String token,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request,
            @ModelAttribute ApplyRequest req,

            @RequestParam(value = "CAREER_COMPANY[]", required = false) List<String> careerCompany1,
            @RequestParam(value = "CAREER_COMPANY", required = false) List<String> careerCompany2,

            @RequestParam(value = "CAREER_DEPARTMENT[]", required = false) List<String> careerDepartment1,
            @RequestParam(value = "CAREER_DEPARTMENT", required = false) List<String> careerDepartment2,

            @RequestParam(value = "CAREER_JOIN_DATE[]", required = false) List<String> careerJoinDate1,
            @RequestParam(value = "CAREER_JOIN_DATE", required = false) List<String> careerJoinDate2,

            @RequestParam(value = "CAREER_RETIRE_DATE[]", required = false) List<String> careerRetireDate1,
            @RequestParam(value = "CAREER_RETIRE_DATE", required = false) List<String> careerRetireDate2,

            @RequestParam(value = "CAREER_POSITION[]", required = false) List<String> careerPosition1,
            @RequestParam(value = "CAREER_POSITION", required = false) List<String> careerPosition2,

            @RequestParam(value = "CAREER_EXPERIENCE[]", required = false) List<String> careerExperience1,
            @RequestParam(value = "CAREER_EXPERIENCE", required = false) List<String> careerExperience2,

            @RequestParam(value = "LICENSE_NAME[]", required = false) List<String> licenseName1,
            @RequestParam(value = "LICENSE_NAME", required = false) List<String> licenseName2,

            @RequestParam(value = "LICENSE_DATE[]", required = false) List<String> licenseDate1,
            @RequestParam(value = "LICENSE_DATE", required = false) List<String> licenseDate2,

            @RequestParam(value = "LICENSE_AGENCY[]", required = false) List<String> licenseAgency1,
            @RequestParam(value = "LICENSE_AGENCY", required = false) List<String> licenseAgency2,

            @RequestParam(value = "LICENSE_NUM[]", required = false) List<String> licenseNum1,
            @RequestParam(value = "LICENSE_NUM", required = false) List<String> licenseNum2,

            @RequestParam(value = "ATTACH_FILE_NM_M113", required = false) MultipartFile fileM113,
            @RequestParam(value = "ATTACH_FILE_NM_M114", required = false) MultipartFile fileM114,
            @RequestParam(value = "ATTACH_FILE_NM_M115", required = false) MultipartFile fileM115
    ) {
        Integer seqNoM100 = authService.requireSeqNoM100(token, authorization, request);

        List<String> careerCompany = (careerCompany2 != null ? careerCompany2 : careerCompany1);
        List<String> careerDepartment = (careerDepartment2 != null ? careerDepartment2 : careerDepartment1);
        List<String> careerJoinDate = (careerJoinDate2 != null ? careerJoinDate2 : careerJoinDate1);
        List<String> careerRetireDate = (careerRetireDate2 != null ? careerRetireDate2 : careerRetireDate1);
        List<String> careerPosition = (careerPosition2 != null ? careerPosition2 : careerPosition1);
        List<String> careerExperience = (careerExperience2 != null ? careerExperience2 : careerExperience1);

        List<String> licenseName = (licenseName2 != null ? licenseName2 : licenseName1);
        List<String> licenseDate = (licenseDate2 != null ? licenseDate2 : licenseDate1);
        List<String> licenseAgency = (licenseAgency2 != null ? licenseAgency2 : licenseAgency1);
        List<String> licenseNum = (licenseNum2 != null ? licenseNum2 : licenseNum1);

        req.setCareerCompany(careerCompany);
        req.setCareerDepartment(careerDepartment);
        req.setCareerJoinDate(careerJoinDate);
        req.setCareerRetireDate(careerRetireDate);
        req.setCareerPosition(careerPosition);
        req.setCareerExperience(careerExperience);

        req.setLicenseName(licenseName);
        req.setLicenseDate(licenseDate);
        req.setLicenseAgency(licenseAgency);
        req.setLicenseNum(licenseNum);

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
            @ModelAttribute ApplyRequest req,

            @RequestParam(value = "CAREER_COMPANY[]", required = false) List<String> careerCompany1,
            @RequestParam(value = "CAREER_COMPANY", required = false) List<String> careerCompany2,

            @RequestParam(value = "CAREER_DEPARTMENT[]", required = false) List<String> careerDepartment1,
            @RequestParam(value = "CAREER_DEPARTMENT", required = false) List<String> careerDepartment2,

            @RequestParam(value = "CAREER_JOIN_DATE[]", required = false) List<String> careerJoinDate1,
            @RequestParam(value = "CAREER_JOIN_DATE", required = false) List<String> careerJoinDate2,

            @RequestParam(value = "CAREER_RETIRE_DATE[]", required = false) List<String> careerRetireDate1,
            @RequestParam(value = "CAREER_RETIRE_DATE", required = false) List<String> careerRetireDate2,

            @RequestParam(value = "CAREER_POSITION[]", required = false) List<String> careerPosition1,
            @RequestParam(value = "CAREER_POSITION", required = false) List<String> careerPosition2,

            @RequestParam(value = "CAREER_EXPERIENCE[]", required = false) List<String> careerExperience1,
            @RequestParam(value = "CAREER_EXPERIENCE", required = false) List<String> careerExperience2,

            @RequestParam(value = "LICENSE_NAME[]", required = false) List<String> licenseName1,
            @RequestParam(value = "LICENSE_NAME", required = false) List<String> licenseName2,

            @RequestParam(value = "LICENSE_DATE[]", required = false) List<String> licenseDate1,
            @RequestParam(value = "LICENSE_DATE", required = false) List<String> licenseDate2,

            @RequestParam(value = "LICENSE_AGENCY[]", required = false) List<String> licenseAgency1,
            @RequestParam(value = "LICENSE_AGENCY", required = false) List<String> licenseAgency2,

            @RequestParam(value = "LICENSE_NUM[]", required = false) List<String> licenseNum1,
            @RequestParam(value = "LICENSE_NUM", required = false) List<String> licenseNum2,

            @RequestParam(value = "ATTACH_FILE_NM_M113", required = false) MultipartFile fileM113,
            @RequestParam(value = "ATTACH_FILE_NM_M114", required = false) MultipartFile fileM114,
            @RequestParam(value = "ATTACH_FILE_NM_M115", required = false) MultipartFile fileM115
    ) {
        Integer seqNoM100 = authService.requireSeqNoM100(token, authorization, request);

        List<String> careerCompany = (careerCompany2 != null ? careerCompany2 : careerCompany1);
        List<String> careerDepartment = (careerDepartment2 != null ? careerDepartment2 : careerDepartment1);
        List<String> careerJoinDate = (careerJoinDate2 != null ? careerJoinDate2 : careerJoinDate1);
        List<String> careerRetireDate = (careerRetireDate2 != null ? careerRetireDate2 : careerRetireDate1);
        List<String> careerPosition = (careerPosition2 != null ? careerPosition2 : careerPosition1);
        List<String> careerExperience = (careerExperience2 != null ? careerExperience2 : careerExperience1);

        List<String> licenseName = (licenseName2 != null ? licenseName2 : licenseName1);
        List<String> licenseDate = (licenseDate2 != null ? licenseDate2 : licenseDate1);
        List<String> licenseAgency = (licenseAgency2 != null ? licenseAgency2 : licenseAgency1);
        List<String> licenseNum = (licenseNum2 != null ? licenseNum2 : licenseNum1);

        req.setCareerCompany(careerCompany);
        req.setCareerDepartment(careerDepartment);
        req.setCareerJoinDate(careerJoinDate);
        req.setCareerRetireDate(careerRetireDate);
        req.setCareerPosition(careerPosition);
        req.setCareerExperience(careerExperience);

        req.setLicenseName(licenseName);
        req.setLicenseDate(licenseDate);
        req.setLicenseAgency(licenseAgency);
        req.setLicenseNum(licenseNum);

        Long jobId = req.getSEQ_NO_M210();
        return ApiResponse.ok(applyService.submit(jobId, seqNoM100, req));
    }
}
