package com.example.job.model;

import jakarta.persistence.*;
import lombok.Data;

// CompanyMember.java - 실제 테이블과 매칭하려면
@Data
@Entity
@Table(name = "T_JB_M200")  // 실제 테이블 이름으로 변경 필요
public class CompanyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEQ_NO_M200")
    private Integer id;

    @Column(name = "COMPANY")
    private String companyName;

    @Column(name = "PRESIDENT_NM")
    private String ceoName;

    @Column(name = "COMPANY_ADDRESS", columnDefinition = "TEXT")
    private String companyAddress;

    @Column(name = "LOGO_PATH")
    private String logoPath;

    // ✅ 추가: 기업 전경 이미지 경로
    @Column(name = "PHOTO_PATH")
    private String photoPath;
}
