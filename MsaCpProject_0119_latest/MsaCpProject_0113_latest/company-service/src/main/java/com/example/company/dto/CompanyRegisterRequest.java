package com.example.company.dto;

import lombok.Data;

@Data
public class CompanyRegisterRequest {
    private String loginId;
    private String pw;
    private String managerNm;
    private String department;
    private String phone;
    private String email;
    private String businessRegistNum;
    private String company;
    private String presidentNm;
    private String companyAddress;
    private String parentCompanyCd;
}