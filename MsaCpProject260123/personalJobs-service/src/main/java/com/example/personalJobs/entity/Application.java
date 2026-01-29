package com.example.personalJobs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "t_jb_m300")
@Getter
@Setter
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_no_m300")
    private Long seqNoM300;

    // 개인회원
    @Column(name = "seq_no_m100", nullable = false)
    private Integer seqNoM100;

    // 이력서
    @Column(name = "seq_no_m110", nullable = false)
    private Long seqNoM110;

    // ✅ 채용공고 (DB 컬럼이 job_id)
    @Column(name = "job_id", nullable = false)
    private Long seqNoM210;

    @Column(name = "review_status")
    private String reviewStatus;

    @Column(name = "cancel_status")
    private String cancelStatus;

    @Column(name = "del_yn")
    private String delYn = "N";
}
