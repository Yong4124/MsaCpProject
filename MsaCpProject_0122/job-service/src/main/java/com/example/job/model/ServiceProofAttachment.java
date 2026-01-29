package com.example.job.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "T_JB_M114")
@Getter
@Setter
public class ServiceProofAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEQ_NO_M114")
    private Long seqNoM114;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEQ_NO_M110")
    private Resume resume;

    @Column(name = "ATTACH_FILE_NM")
    private String attachFileNm;

    @Column(name = "FILE_PATH")
    private String filePath;

    @Column(name = "DEL_YN")
    private String delYn = "N";
}