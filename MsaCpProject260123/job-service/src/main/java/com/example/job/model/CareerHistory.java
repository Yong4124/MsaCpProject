package com.example.job.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "T_JB_M111")
@Getter
@Setter
public class CareerHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEQ_NO_M111")
    private Long seqNoM111;

    // FK: SEQ_NO_M110
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEQ_NO_M110")
    private Resume resume;

    @Column(name = "COMPANY")
    private String company;

    @Column(name = "DEPARTMENT")
    private String department;

    @Column(name = "JOIN_DATE")
    private String joinDate;

    @Column(name = "RETIRE_DATE")
    private String retireDate;

    @Column(name = "POSITION")
    private String position;

    @Column(name = "SALARY")
    private String salary;

    @Lob
    @Column(name = "POSITION_SUMMARY", columnDefinition = "TEXT")
    private String positionSummary;

    @Lob
    @Column(name = "EXPERIENCE", columnDefinition = "TEXT")
    private String experience;

    @Column(name = "DEL_YN")
    private String delYn = "N";
}
