package com.example.personal.dto;

import com.example.personal.model.*;
import lombok.Data;

import java.time.LocalDate;

// 회원정보 조회
@Data
public class MemberInfoDto {

    // 기본 정보
    private String loginId;
    private String name;
    private LocalDate birthDate;
    private Gender gender;
    private String email;
    private Residence residence;
    private String lastRank;
    private ServiceCategory serviceCategory;
    private ServiceBranch serviceBranch;
    private String serviceYear;
    private String serviceStation;
    private String unitPosition;

    // entitiy를 DTO로 변환
    public static MemberInfoDto fromEntity(Personal personal) {
        MemberInfoDto dto = new MemberInfoDto();

        dto.setLoginId(personal.getLoginId());
        dto.setName(personal.getName());
        dto.setBirthDate(personal.getBirthDate());
        dto.setGender(personal.getGender());
        dto.setResidence(personal.getResidence());
        dto.setLastRank(personal.getLastRank());
        dto.setServiceCategory(personal.getServiceCategory());
        dto.setServiceBranch(personal.getServiceBranch());
        dto.setServiceYear(personal.getServiceYear());
        dto.setServiceStation(personal.getServiceStation());
        dto.setUnitPosition(personal.getUnitPosition());
        dto.setEmail(personal.getEmail());

        return dto;
    }
}
