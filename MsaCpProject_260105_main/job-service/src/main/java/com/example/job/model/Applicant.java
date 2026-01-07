package com.example.job.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "T_JB_M300")
@Getter @Setter
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEQ_NO_M300")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEQ_NO_M210")
    @JsonIgnore
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEQ_NO_M110")
    private Resume resume;

    @Column(name = "SEQ_NO_M100")
    private Long seqNoM100;

    @Column(name = "REVIEW_STATUS")
    private String status = "0"; // 0=미심사

    @Column(name = "CANCEL_STATUS")
    private String cancelStatus = "N";

    @Column(name = "DEL_YN")
    private String delYn = "N";
}