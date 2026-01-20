package com.example.personalJobs.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ApplyRequest {

    // ✅ apply.html hidden input name="SEQ_NO_M210" 들어오는 값
    private Long SEQ_NO_M210;

    // A110
    private String phone;
    private String email;
    private String address;
    private String school;
    private String major;
    private String selfIntro;

    // ✅ Controller가 주입하는 배열 파라미터들
    private List<String> careerCompany;
    private List<String> careerDepartment;
    private List<String> careerJoinDate;
    private List<String> careerRetireDate;
    private List<String> careerPosition;
    private List<String> careerExperience;

    private List<String> licenseName;
    private List<String> licenseDate;
    private List<String> licenseAgency;
    private List<String> licenseNum;
    private List<Career> careers;
    private List<License> licenses;


    // =========================
    // Service에서 쓰기 편하게 변환
    // =========================
    public List<Career> toCareers() {
        // ✅ ResumeService가 넣어준 값이 있으면 그걸 우선 사용
        if (careers != null) return careers;

        List<Career> out = new ArrayList<>();
        int n = safeSize(careerCompany);

        for (int i = 0; i < n; i++) {
            String company = get(careerCompany, i);
            String dept = get(careerDepartment, i);
            String join = get(careerJoinDate, i);
            String retire = get(careerRetireDate, i);
            String pos = get(careerPosition, i);
            String exp = get(careerExperience, i);

            if (isAllBlank(company, dept, join, retire, pos, exp)) continue;

            Career c = new Career();
            c.setCompany(company);
            c.setDepartment(dept);
            c.setJoinDate(join);
            c.setRetireDate(retire);
            c.setPosition(pos);
            c.setExperience(exp);
            out.add(c);
        }
        return out;
    }

    public List<License> toLicenses() {
        // ✅ ResumeService가 넣어준 값이 있으면 그걸 우선 사용
        if (licenses != null) return licenses;

        List<License> out = new ArrayList<>();
        int n = safeSize(licenseName);

        for (int i = 0; i < n; i++) {
            String nm = get(licenseName, i);
            String dt = get(licenseDate, i);
            String ag = get(licenseAgency, i);
            String num = get(licenseNum, i);

            if (isAllBlank(nm, dt, ag, num)) continue;

            License l = new License();
            l.setCertificateNm(nm);
            l.setObtainDate(dt);
            l.setAgency(ag);
            l.setCertificateNum(num);
            out.add(l);
        }
        return out;
    }


    private int safeSize(List<String> a) {
        return a == null ? 0 : a.size();
    }

    private String get(List<String> a, int i) {
        if (a == null) return null;
        if (i < 0 || i >= a.size()) return null;
        return a.get(i);
    }

    private boolean isAllBlank(String... ss) {
        for (String s : ss) {
            if (s != null && !s.trim().isEmpty()) return false;
        }
        return true;
    }

    // =========================
    // 내부 구조(서비스에서 사용)
    // =========================
    @Getter
    @Setter
    public static class Career {
        private String company;
        private String department;
        private String joinDate;
        private String retireDate;
        private String position;
        private String experience;
    }

    @Getter
    @Setter
    public static class License {
        private String certificateNm;
        private String obtainDate;
        private String agency;
        private String certificateNum;
    }
}
