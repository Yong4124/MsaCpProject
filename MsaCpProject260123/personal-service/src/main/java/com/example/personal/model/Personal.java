package com.example.personal.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "T_JB_M100")
@Data
public class Personal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEQ_NO_M100")
    private Integer seqNoM100;

    @Column(name = "ID", nullable = false, length = 100)
    private String loginId;

    @Column(name = "PW", nullable = false, length = 500)
    private String pw;

    @Column(name = "SALT", nullable = false, length = 100)
    private String salt;

    @Column(name = "NAME", nullable = false, length = 500)
    private String name;

    @Column(name = "BIRTH_DATE", nullable = false)
    private LocalDate birthDate;

    @Column(name = "GENDER", nullable = false,  columnDefinition = "VARCHAR(1)")
    @Enumerated(EnumType.STRING)
    private Gender gender; // M, F, O, N

    @Column(name = "EMAIL", nullable = false, length = 100)
    private String email;

    @Column(name = "RESIDENCE",  columnDefinition = "VARCHAR(1)")
    @Enumerated(EnumType.STRING)
    private Residence residence; // O, K, U

    @Column(name = "LAST_RANK", length = 500)
    private String lastRank;

    @Column(name = "SERVICE_CATEGORY", length = 100)
    @Enumerated(EnumType.STRING)
    private ServiceCategory serviceCategory;

    @Column(name = "SERVICE_BRANCH", length = 100)
    @Enumerated(EnumType.STRING)
    private ServiceBranch serviceBranch;

    @Column(name = "SERVICE_YEAR", columnDefinition = "TEXT")
    private String serviceYear;

    @Column(name = "SERVICE_STATION", columnDefinition = "TEXT")
    private String serviceStation;

    @Column(name = "UNIT_POSITION", length = 100)
    private String unitPosition;

    @Column(name = "INSERT_DATE")
    private LocalDate insertDate = LocalDate.now();

    @Column(name = "APPROVAL_YN", nullable = false,  columnDefinition = "VARCHAR(1)")
    @Enumerated(EnumType.STRING)
    private ApprovalYn approvalYn = ApprovalYn.N; // Y, N

    @Column(name = "DEL_YN", nullable = false,  columnDefinition = "VARCHAR(1)")
    @Enumerated(EnumType.STRING)
    private DelYn delYn = DelYn.N; // Y, N
}