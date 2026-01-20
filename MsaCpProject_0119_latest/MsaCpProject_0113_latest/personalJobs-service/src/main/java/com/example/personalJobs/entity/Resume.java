package com.example.personalJobs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "T_JB_M110")
@Getter
@Setter
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEQ_NO_M110")
    private Long seqNoM110;

    @Column(name = "SEQ_NO_M100")
    private Long seqNoM100;

    @Column(name = "NAME")
    private String name;

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "BIRTH_DATE")
    private String birthDate;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "ADDRESS")
    private String address;

    // [최종학력]
    @Column(name = "SCHOOL")
    private String schoolName;

    @Column(name = "MAJOR")
    private String major;

    @Column(name = "ENROLL_DATE")
    private String entranceDate;

    @Column(name = "GRADUATE_DATE")
    private String gradDate;

    @Column(name = "GPA")
    private String score;

    @Column(name = "GRADUATE_STATUS")
    private String gradStatus;

    // ✨ [경력]
    @Column(name = "COMPANY")
    private String company;

    @Column(name = "DEPT")
    private String dept;

    @Column(name = "JOIN_DATE")
    private String joinDate;

    @Column(name = "LEAVE_DATE")
    private String leaveDate;

    @Column(name = "POSITION")
    private String position;

    @Column(name = "SALARY")
    private String salary;

    @Lob
    @Column(name = "TASK", columnDefinition = "TEXT")
    private String task;

    @Lob
    @Column(name = "CAREER_DESC", columnDefinition = "TEXT")
    private String careerDesc;

    // [전문분야]
    @Lob
    @Column(name = "SKILL", columnDefinition = "TEXT")
    private String speciality;

    // [자기소개]
    @Lob
    @Column(name = "SELF_INTRO", columnDefinition = "TEXT")
    private String introduction;

    // [자격증] - Certificate 엔티티와 관계
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<Certificate> certificates = new ArrayList<>();

    // ✨ [복무증명서 첨부파일] - T_JB_M114
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<ServiceProofAttachment> serviceProofAttachments = new ArrayList<>();

    // ✨ [이력서 첨부파일] - T_JB_M115
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<ResumeFileAttachment> resumeFileAttachments = new ArrayList<>();

    @Column(name = "INSERT_DATE")
    private String insertDate;

    @Column(name = "APPLY_YN")
    private String applyYn = "N";

    @Column(name = "DEL_YN")
    private String delYn = "N";
}