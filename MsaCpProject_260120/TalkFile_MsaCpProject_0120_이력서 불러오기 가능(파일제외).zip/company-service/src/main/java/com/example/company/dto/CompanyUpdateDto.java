package com.example.company.dto;

import com.example.company.model.ParentCompany;
import lombok.Data;

/**
 * 회원정보 수정용 DTO
 */
@Data
public class CompanyUpdateDto {

    private String managerNm;
    private String department;
    private String phone;
    private String email;
    private String businessRegistNum;
    private String company;
    private String presidentNm;
    private String companyAddress;
    private ParentCompany parentCompanyCd;
    
    // 비밀번호 변경용
    private String newPassword;
}
