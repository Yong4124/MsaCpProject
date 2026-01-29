package com.example.personalJobs.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ApplyRequest {

    // ✅ apply.html hidden input name="SEQ_NO_M210"
    private Long SEQ_NO_M210;

    // 기본
    private String name;
    private String gender;
    private String birthDate;
    private String phone;
    private String email;
    private String address;
    private String school;
    private String major;
    private String selfIntro;

    // 최종학력/기술
    private String enrollDate;
    private String graduateDate;
    private String gpa;
    private String graduateStatus;
    private String skill;

    // 배열(프론트 KEY_MAP으로 들어옴)
    private List<String> careerCompany;
    private List<String> careerDepartment;
    private List<String> careerJoinDate;
    private List<String> careerRetireDate;
    private List<String> careerPosition;
    private List<String> careerSalary;
    private List<String> careerPositionSummary; // 담당업무
    private List<String> careerExperience;      // 경력기술서

    private List<String> licenseName;
    private List<String> licenseDate;
    private List<String> licenseAgency;
    private List<String> licenseNum;

    // 서비스가 채워서 내려줄 수도 있는 구조
    private List<Career> careers;
    private List<License> licenses;

    // 파일 (폼 name 그대로) - 업로드용
    private MultipartFile ATTACH_FILE_NM_M113; // 사진
    private MultipartFile ATTACH_FILE_NM_M114; // 복무증명서
    private MultipartFile ATTACH_FILE_NM_M115; // 이력서 파일

    // 첨부파일 정보 (불러오기용)
    private String photoPath;                              // 사진 경로
    private List<AttachmentInfo> serviceProofFiles;        // 복무증명서 파일 목록
    private List<AttachmentInfo> resumeFiles;              // 이력서 첨부파일 목록

    // =========================
    // Service에서 쓰기 편하게 변환
    // =========================
    public List<Career> toCareers() {
        if (careers != null) return careers;

        List<Career> out = new ArrayList<>();

        int n = maxSize(
                careerCompany,
                careerDepartment,
                careerJoinDate,
                careerRetireDate,
                careerPosition,
                careerSalary,
                careerPositionSummary,
                careerExperience
        );

        for (int i = 0; i < n; i++) {
            String company = get(careerCompany, i);
            String dept = get(careerDepartment, i);
            String join = get(careerJoinDate, i);
            String retire = get(careerRetireDate, i);
            String pos = get(careerPosition, i);
            String salary = get(careerSalary, i);
            String duty = get(careerPositionSummary, i);
            String exp = get(careerExperience, i);

            if (isAllBlank(company, dept, join, retire, pos, salary, duty, exp)) continue;

            Career c = new Career();
            c.setCompany(company);
            c.setDepartment(dept);
            c.setJoinDate(join);
            c.setRetireDate(retire);
            c.setPosition(pos);
            c.setSalary(salary);
            c.setPositionSummary(duty);
            c.setExperience(exp);

            out.add(c);
        }

        return out;
    }

    public List<License> toLicenses() {
        if (licenses != null) return licenses;

        List<License> out = new ArrayList<>();

        int n = maxSize(licenseName, licenseDate, licenseAgency, licenseNum);

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

    private int size(List<String> a) {
        return a == null ? 0 : a.size();
    }

    private int maxSize(List<String>... lists) {
        int m = 0;
        if (lists == null) return 0;
        for (List<String> l : lists) {
            m = Math.max(m, size(l));
        }
        return m;
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

        private String salary;
        private String positionSummary;

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

    // 첨부파일 정보 DTO (불러오기용)
    @Getter
    @Setter
    public static class AttachmentInfo {
        private Long id;
        private String fileName;
        private String filePath;
    }
}
