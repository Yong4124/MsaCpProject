package com.example.company.dto;

import com.example.company.model.Company;
import com.example.company.model.ParentCompany;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * 회원정보 조회용 DTO (비밀번호, Salt 제외)
 */
@Data
@Builder
public class CompanyInfoDto {

    private Integer seqNoM200;
    private String loginId;
    private String managerNm;
    private String department;
    private String phone;
    private String email;
    private String businessRegistNum;
    private String company;
    private String presidentNm;
    private String companyAddress;
    private ParentCompany parentCompanyCd;
    private LocalDate insertDate;
    private String approvalYn;

    /**
     * Entity -> DTO 변환
     */
    public static CompanyInfoDto fromEntity(Company company) {
        return CompanyInfoDto.builder()
                .seqNoM200(company.getSeqNoM200())
                .loginId(company.getLoginId())
                .managerNm(company.getManagerNm())
                .department(company.getDepartment())
                .phone(company.getPhone())
                .email(company.getEmail())
                .businessRegistNum(company.getBusinessRegistNum())
                .company(company.getCompany())
                .presidentNm(company.getPresidentNm())
                .companyAddress(company.getCompanyAddress())
                .parentCompanyCd(company.getParentCompanyCd())
                .insertDate(company.getInsertDate())
                .approvalYn(company.getApprovalYn().name())
                .build();
    }
}
