package com.example.personalJobs.entity;

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

    @Column(name = "EMAIL", nullable = false, length = 100)
    private String email;

}