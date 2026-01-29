package com.example.personalJobs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "T_JB_M113")   // 사진첨부테이블임
@Getter
@Setter
public class ResumeAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEQ_NO_M113")
    private Long seqNoM113;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEQ_NO_M110")
    private Resume resume;

    @Column(name = "FILE_TYPE")
    private String fileType;           // 파일 유형: "SERVICE_PROOF" (복무증명서) 또는 "RESUME" (이력서)

    @Column(name = "FILE_NAME")
    private String fileName;           // 원본 파일명

    @Column(name = "FILE_PATH")
    private String filePath;           // 저장된 파일 경로

    @Column(name = "FILE_SIZE")
    private Long fileSize;             // 파일 크기 (bytes)

    @Column(name = "UPLOAD_DATE")
    private String uploadDate;         // 업로드 날짜

    @Column(name = "DEL_YN")
    private String delYn = "N";
}