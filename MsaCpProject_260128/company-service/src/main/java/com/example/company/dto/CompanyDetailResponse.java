package com.example.company.dto;

import com.example.company.model.Company;
import com.example.company.model.ParentCompany;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Admin 상세 조회용 Response DTO
 */
@Data
@Builder
public class CompanyDetailResponse {

    private Integer no;
    private String loginId;
    private String managerNm;
    private String department;
    private String phone;
    private String email;
    private String businessRegistNum;
    private String company;
    private String presidentNm;
    private String companyAddress;
    private String parentCompanyCd;
    private String parentCompanyNm;
    private LocalDate insertDate;
    private String approvalYn;

    /**
     * Entity -> Response 변환
     */
    public static CompanyDetailResponse from(Company c) {
        return CompanyDetailResponse.builder()
                .no(c.getSeqNoM200())
                .loginId(c.getLoginId())
                .managerNm(c.getManagerNm())
                .department(c.getDepartment())
                .phone(c.getPhone())
                .email(c.getEmail())
                .businessRegistNum(c.getBusinessRegistNum())
                .company(c.getCompany())
                .presidentNm(c.getPresidentNm())
                .companyAddress(c.getCompanyAddress())
                .parentCompanyCd(c.getParentCompanyCd() != null ? c.getParentCompanyCd().name() : null)
                .parentCompanyNm(c.getParentCompanyCd() != null ? c.getParentCompanyCd().getDisplayName() : null)
                .insertDate(c.getInsertDate())
                .approvalYn(c.getApprovalYn().name())
                .build();
    }
}
