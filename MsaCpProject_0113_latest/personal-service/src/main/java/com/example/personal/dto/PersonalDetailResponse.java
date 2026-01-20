package com.example.personal.dto;

import com.example.personal.model.Personal;
import com.example.personal.model.ServiceStation;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

// admin
@Getter
@Setter
public class PersonalDetailResponse {

    private Integer seqNoM100;
    private String approvalYn;
    private String loginId;
    private String name;
    private LocalDate birthDate;
    private String gender;
    private String email;
    private String residence;
    private String lastRank;
    private String serviceBranch;
    private String serviceCategory;

    // ✅ 화면에는 라벨로 내려줌
    private String serviceStation; // ex) "Camp Carroll, Camp Casey" 처럼
    private String serviceYear;    // ex) "1952, 1953, 1954, 2023"
    private String unitPosition;

    public static PersonalDetailResponse from(Personal p) {
        PersonalDetailResponse r = new PersonalDetailResponse();

        r.seqNoM100 = p.getSeqNoM100();
        r.approvalYn = (p.getApprovalYn() == null) ? null : p.getApprovalYn().name();

        r.loginId = p.getLoginId();
        r.name = p.getName();
        r.birthDate = p.getBirthDate();

        r.gender = (p.getGender() == null) ? null : p.getGender().name();
        r.email = p.getEmail();
        r.residence = (p.getResidence() == null) ? null : p.getResidence().name();

        r.lastRank = p.getLastRank();

        // enum 이름 보기 좋게 (US_DOD_PERSONNEL -> US DOD PERSONNEL)
        r.serviceBranch = (p.getServiceBranch() == null) ? null : prettyEnum(p.getServiceBranch().name());
        r.serviceCategory = (p.getServiceCategory() == null) ? null : prettyEnum(p.getServiceCategory().name());

        // ✅ DB에 코드로 저장된 값들 변환
        r.serviceStation = toServiceStationLabel(p.getServiceStation()); // "1|2|3" -> "..."
        r.serviceYear = toPipeListLabel(p.getServiceYear());             // "1952|1953" -> "1952, 1953"

        r.unitPosition = p.getUnitPosition();
        return r;
    }

    /**
     * enum 이름을 보기 좋게 변환
     * 예) US_DOD_PERSONNEL -> US DOD PERSONNEL
     * (원하면 타이틀케이스로도 바꿔줄 수 있음)
     */
    private static String prettyEnum(String raw) {
        if (raw == null) return null;
        return raw.replace('_', ' ');
    }

    /**
     * "1952|1953|1954" -> "1952, 1953, 1954"
     * 공통 파이프 구분 문자열 변환
     */
    private static String toPipeListLabel(String raw) {
        if (raw == null) return null;
        return Arrays.stream(raw.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
    }

    /**
     * serviceStation 코드 변환:
     * - DB: "4" 또는 "1|2|3"
     * - 화면: "CAMP_..." 라벨 목록으로 변환
     *
     * ⚠️ 여기서 "숫자코드"는 ServiceStation.values()의 순서를 따른다고 가정(1부터 시작).
     * 만약 DB 코드 규칙이 다르면 알려줘야 정확히 매핑 가능.
     */
    private static String toServiceStationLabel(String raw) {
        if (raw == null) return null;

        ServiceStation[] vals = ServiceStation.values();

        return Arrays.stream(raw.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(code -> {
                    // 숫자 코드면 values()[code-1]로 매핑
                    if (code.matches("\\d+")) {
                        int idx = Integer.parseInt(code);
                        if (idx >= 1 && idx <= vals.length) {
                            return prettyEnum(vals[idx - 1].name());
                        }
                        return code; // 범위 밖이면 그대로
                    }

                    // 혹시 DB에 enum 문자열이 저장된 경우도 대응
                    try {
                        return prettyEnum(ServiceStation.valueOf(code).name());
                    } catch (Exception e) {
                        return code;
                    }
                })
                .collect(Collectors.joining(", "));
    }
}
