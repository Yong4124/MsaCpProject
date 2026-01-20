package com.example.admin.model;

import java.util.Arrays;


public enum AdminRole {
    SUPER_ADMIN("1", "슈퍼관리자"),
    KCCI_ADMIN("2", "대한상공회의소 관리자"),
    KUSAF_ADMIN("3", "한미동맹재단 관리자");

    private final String code;
    private final String label;

    AdminRole(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }

    public static AdminRole fromCode(String code) {
        return Arrays.stream(values())
                .filter(r -> r.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown role code: " + code));
    }

    // ✅ 추가: 개인회원 승인 가능 여부
    public boolean canApprovePersonal() {
        return this == SUPER_ADMIN || this == KUSAF_ADMIN;
    }
}