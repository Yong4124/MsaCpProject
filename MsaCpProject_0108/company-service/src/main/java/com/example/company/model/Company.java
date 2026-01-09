package com.example.company.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 기업회원 정보 테이블 (T_JB_M200)
 * 원본 DDL 기준
 */
@Entity
@Table(name = "T_JB_M200")
@Data
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEQ_NO_M200")
    private Integer seqNoM200;

    // 로그인 ID (담당자 아이디)
    @Column(name = "ID", nullable = false, length = 100)
    private String loginId;

    // 비밀번호
    @Column(name = "PW", nullable = false, length = 500)
    private String pw;

    // Salt (비밀번호 암호화용)
    @Column(name = "SALT", nullable = false, length = 100)
    private String salt;

    // 담당자명
    @Column(name = "MANAGER_NM", nullable = false, length = 500)
    private String managerNm;

    // 부서
    @Column(name = "DEPARTMENT", length = 500)
    private String department;

    // 전화번호
    @Column(name = "PHONE", nullable = false, length = 100)
    private String phone;

    // 이메일
    @Column(name = "EMAIL", nullable = false, length = 100)
    private String email;

    // 사업자등록번호
    @Column(name = "BUSINESS_REGIST_NUM", nullable = false, length = 500)
    private String businessRegistNum;

    // 회사명
    @Column(name = "COMPANY", nullable = false, length = 500)
    private String company;

    // 대표자명
    @Column(name = "PRESIDENT_NM", nullable = false, length = 500)
    private String presidentNm;

    // 회사 주소(본사)
    @Column(name = "COMPANY_ADDRESS", nullable = false, columnDefinition = "TEXT")
    private String companyAddress;

    // 그룹사 코드
    @Column(name = "PARENT_COMPANY_CD", length = 100)
    @Enumerated(EnumType.STRING)
    private ParentCompany parentCompanyCd;

    // 가입일
    @Column(name = "INSERT_DATE", nullable = false)
    private LocalDate insertDate = LocalDate.now();

    // 승인여부 Y/N
    @Column(name = "APPROVAL_YN", nullable = false, columnDefinition = "VARCHAR(1)")
    @Enumerated(EnumType.STRING)
    private ApprovalYn approvalYn = ApprovalYn.N;

    // 삭제여부 Y/N
    @Column(name = "DEL_YN", nullable = false, columnDefinition = "VARCHAR(1)")
    @Enumerated(EnumType.STRING)
    private DelYn delYn = DelYn.N;

    // 기업로고 경로
    @Column(name = "LOGO_PATH", length = 500)
    private String logoPath;

    // 기업전경 사진 경로
    @Column(name = "PHOTO_PATH", length = 500)
    private String photoPath;
}
