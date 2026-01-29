package com.example.company.model;

/**
 * 그룹사 코드
 * 원본 사이트의 그룹사 선택 옵션
 */
public enum ParentCompany {
    SAMSUNG("삼성"),
    SK("SK"),
    HYUNDAI("현대"),
    LG("LG"),
    LOTTE("롯데"),
    HANWHA("한화"),
    LS("LS"),
    DL("DL"),
    DOOSAN("두산"),
    HANKOOKTIRE("한국타이어"),
    HYOSUNG("효성"),
    KUMHOTIRE("금호타이어"),
    SOULBRAIN("솔브레인"),
    SD("SD"),
    ETC("기타");

    private final String displayName;

    ParentCompany(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
