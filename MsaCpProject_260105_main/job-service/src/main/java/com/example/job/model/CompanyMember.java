package com.example.job.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "T_JB_M200")
@Getter
@Setter// 핵심: 아래 어노테이션은 Hibernate가 이 엔티티를 위해 DDL(CREATE/ALTER)을 생성하는 것을 막습니다.
@org.hibernate.annotations.Subselect("select * from T_JB_M200")
public class CompanyMember {
    @Id
    @Column(name = "SEQ_NO_M200")
    private Integer id;

    @Column(name = "ID", insertable = false, updatable = false)
    private String memberId;

    // ⭐ 비밀번호, Salt는 job-service에서 필요 없으므로 제거

    @Column(name = "MANAGER_NM", insertable = false, updatable = false)
    private String managerNm;

    @Column(name = "DEPARTMENT", insertable = false, updatable = false)
    private String department;

    @Column(name = "PHONE", insertable = false, updatable = false)
    private String phone;

    @Column(name = "EMAIL", insertable = false, updatable = false)
    private String email;

    @Column(name = "BUSINESS_REGIST_NUM", insertable = false, updatable = false)
    private String businessNo;

    @Column(name = "COMPANY", insertable = false, updatable = false)
    private String companyName;

    @Column(name = "PRESIDENT_NM", insertable = false, updatable = false)
    private String ceoName;

    @Column(name = "COMPANY_ADDRESS", insertable = false, updatable = false)
    private String companyAddress;

    @Column(name = "APPROVAL_YN", insertable = false, updatable = false)
    private String approvalYn;

    @Column(name = "DEL_YN", insertable = false, updatable = false)
    private String delYn;
}