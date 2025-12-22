package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resume")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_member_id", nullable = false)
    private PersonalMember personalMember;

    @Column(length = 100)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 500)
    private String school;  // 학교

    @Column(length = 500)
    private String major;  // 전공명

    @Column(name = "enroll_date", length = 100)
    private String enrollDate;  // 입학년월

    @Column(name = "graduate_date", length = 100)
    private String graduateDate;  // 졸업년월

    @Column(length = 500)
    private String gpa;  // 학점

    @Column(name = "graduate_status", length = 1)
    private String graduateStatus;  // 1: 졸업예정, 2: 재학중, 3: 중퇴, 4: 수료, 5: 휴학

    @Column(columnDefinition = "TEXT")
    private String skill;  // 전문분야

    @Column(name = "self_intro", columnDefinition = "TEXT")
    private String selfIntro;  // 자기소개서

    @Column(name = "insert_date")
    private LocalDate insertDate;

    @Column(name = "update_date")
    private LocalDate updateDate;

    @Column(name = "apply_yn", nullable = false, length = 1)
    @Builder.Default
    private String applyYn = "2";  // 1: 지원완료, 2: 임시저장

    @Column(name = "del_yn", nullable = false, length = 1)
    @Builder.Default
    private String delYn = "N";

    // 연관관계
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ResumeCareer> careers = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ResumeCertificate> certificates = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ResumePhoto> photos = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ResumeServiceDoc> serviceDocs = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ResumeAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Application> applications = new ArrayList<>();
}
