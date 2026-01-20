package com.example.job.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "T_JB_M112")
@Getter
@Setter
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEQ_NO_M112")
    private Long seqNoM112;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEQ_NO_M110")
    private Resume resume;

    @Column(name = "CERTIFICATE_NM")
    private String certificateNm;      // 자격/기술명

    @Column(name = "OBTAIN_DATE")
    private String obtainDate;         // 취득년월

    @Column(name = "AGENCY")
    private String agency;             // 발급기관

    @Column(name = "CERTIFICATE_NUM")
    private String certificateNum;     // 자격증 번호

    @Column(name = "DEL_YN")
    private String delYn = "N";
}