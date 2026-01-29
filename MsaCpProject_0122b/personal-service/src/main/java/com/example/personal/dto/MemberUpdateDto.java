package com.example.personal.dto;

import com.example.personal.model.Gender;
import com.example.personal.model.Residence;
import com.example.personal.model.ServiceBranch;
import com.example.personal.model.ServiceCategory;
import lombok.Data;

import java.time.LocalDate;

// 회원정보 수정
@Data
public class MemberUpdateDto {

    private Gender gender;
    private String email;
    private Residence residence;
    private String  lastRank;
    private ServiceCategory serviceCategory;
    private ServiceBranch serviceBranch;
    private String serviceYear;
    private String serviceStation;
    private String unitPosition;
    private String newPassword;
}
