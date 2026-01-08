package com.example.company.dto;

import lombok.Data;

@Data
public class CompanyLoginRequest {
    private String loginId;
    private String pw;
}