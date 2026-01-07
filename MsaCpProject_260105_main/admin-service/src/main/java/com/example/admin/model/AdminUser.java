package com.example.admin.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "T_JB_C010",
        uniqueConstraints = @UniqueConstraint(name = "UK_T_JB_C010_ID", columnNames = "ID")
)
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEQ_NO_C010")
    private Long seqNoC010;

    @Column(name = "ID", length = 100, nullable = false)
    private String loginId;

    @Column(name = "PW", length = 255, nullable = false)
    private String pw;

    @Column(name = "NM", length = 100, nullable = false)
    private String nm;

    @Column(name = "EMAIL", length = 100, nullable = false)
    private String email;

    @Column(name = "DEPARTMENT", length = 500)
    private String department;

    @Column(name = "PHONE", length = 100, nullable = false)
    private String phone;

    // AUTH: 1/2/3 (CHAR(1))
    @Column(name = "AUTH", length = 1, nullable = false)
    private String auth;

    @Column(name = "INSERT_DATE")
    private LocalDate insertDate;

    // DEL_YN: Y/N
    @Column(name = "DEL_YN", length = 1, nullable = false)
    private String delYn = "N";

    public AdminUser() {}

    // --- getters/setters ---
    public Long getSeqNoC010() { return seqNoC010; }
    public void setSeqNoC010(Long seqNoC010) { this.seqNoC010 = seqNoC010; }

    public String getLoginId() { return loginId; }
    public void setLoginId(String id) { this.loginId = id; }

    public String getPw() { return pw; }
    public void setPw(String pw) { this.pw = pw; }

    public String getNm() { return nm; }
    public void setNm(String nm) { this.nm = nm; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAuth() { return auth; }
    public void setAuth(String auth) { this.auth = auth; }

    public LocalDate getInsertDate() { return insertDate; }
    public void setInsertDate(LocalDate insertDate) { this.insertDate = insertDate; }

    public String getDelYn() { return delYn; }
    public void setDelYn(String delYn) { this.delYn = delYn; }

    // 편의 메서드
    public AdminRole getRole() { return AdminRole.fromCode(this.auth); }
    public void setRole(AdminRole role) { this.auth = role.getCode(); }
}

