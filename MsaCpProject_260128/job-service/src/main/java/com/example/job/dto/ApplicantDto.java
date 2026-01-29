package com.example.job.dto;

import com.example.job.model.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ApplicantDto {
    private Long id;
    private Long jobId;
    private Long resumeId;
    private String status;

    // 인적사항
    private String photoPath;
    private String name;
    private String gender;
    private String birthDate;
    private String phone;
    private String email;
    private String address;

    // 학력
    private String schoolName;
    private String major;
    private String entranceDate;
    private String gradDate;
    private String score;
    private String gradStatus;

    // ✨ 경력 이력 (복수)
    private List<CareerHistoryDto> careerHistories = new ArrayList<>();

    // 전문분야
    private String speciality;

    // 자기소개
    private String introduction;

    // 자격증
    private List<CertificateDto> certificates = new ArrayList<>();

    // ✨ 복무증명서 첨부파일
    private List<ServiceProofDto> serviceProofFiles = new ArrayList<>();

    // ✨ 이력서 첨부파일
    private List<ResumeFileDto> resumeFiles = new ArrayList<>();

    /**
     * Entity -> DTO 변환
     */
    public static ApplicantDto from(Applicant applicant) {
        ApplicantDto dto = new ApplicantDto();
        dto.setId(applicant.getId());

        // 채용공고 ID 설정
        if (applicant.getJob() != null) {
            dto.setJobId(applicant.getJob().getSeqNoM210());
        }

        dto.setStatus(applicant.getStatus());

        Resume resume = applicant.getResume();
        if (resume != null) {
            dto.setResumeId(resume.getSeqNoM110());

            // [인적사항]
            dto.setPhotoPath(resume.getPhotoPath());
            dto.setName(resume.getName());
            dto.setGender(resume.getGender());
            dto.setBirthDate(resume.getBirthDate());
            dto.setPhone(resume.getPhone());
            dto.setEmail(resume.getEmail());
            dto.setAddress(resume.getAddress());

            // ✅✅✅ 사진 경로 설정 (확인용 로그)
            String photoPath = resume.getPhotoPath();
            System.out.println(">>> from() 메서드 내부 - Resume photoPath: [" + photoPath + "]");
            dto.setPhotoPath(photoPath);
            System.out.println(">>> from() 메서드 내부 - DTO photoPath 설정 후: [" + dto.getPhotoPath() + "]");

            // [최종학력]
            dto.setSchoolName(resume.getSchoolName());
            dto.setMajor(resume.getMajor());
            dto.setEntranceDate(resume.getEntranceDate());
            dto.setGradDate(resume.getGradDate());
            dto.setScore(resume.getScore());
            dto.setGradStatus(resume.getGradStatus());

            // ✨ [경력 이력] - CareerHistory 리스트로 변환
            if (resume.getCareerHistories() != null) {
                dto.setCareerHistories(
                        resume.getCareerHistories().stream()
                                .filter(ch -> !"Y".equals(ch.getDelYn()))
                                .map(CareerHistoryDto::from)
                                .collect(Collectors.toList())
                );
            }

            // [전문분야]
            dto.setSpeciality(resume.getSpeciality());

            // [자기소개]
            dto.setIntroduction(resume.getIntroduction());

            // [자격증]
            if (resume.getCertificates() != null) {
                dto.setCertificates(
                        resume.getCertificates().stream()
                                .filter(cert -> !"Y".equals(cert.getDelYn()))
                                .map(CertificateDto::from)
                                .collect(Collectors.toList())
                );
            }

            // ✨ [복무증명서 첨부파일]
            if (resume.getServiceProofAttachments() != null) {
                dto.setServiceProofFiles(
                        resume.getServiceProofAttachments().stream()
                                .filter(sp -> !"Y".equals(sp.getDelYn()))
                                .map(ServiceProofDto::from)
                                .collect(Collectors.toList())
                );
            }

            // ✨ [이력서 첨부파일]
            if (resume.getResumeFileAttachments() != null) {
                dto.setResumeFiles(
                        resume.getResumeFileAttachments().stream()
                                .filter(rf -> !"Y".equals(rf.getDelYn()))
                                .map(ResumeFileDto::from)
                                .collect(Collectors.toList())
                );
            }
        }

        return dto;
    }

    // ✨ 경력 이력 DTO
    @Data
    public static class CareerHistoryDto {
        private Long id;
        private String company;
        private String department;
        private String joinDate;
        private String retireDate;
        private String position;
        private String salary;
        private String positionSummary;
        private String experience;

        public static CareerHistoryDto from(CareerHistory careerHistory) {
            CareerHistoryDto dto = new CareerHistoryDto();
            dto.setId(careerHistory.getSeqNoM111());
            dto.setCompany(careerHistory.getCompany());
            dto.setDepartment(careerHistory.getDepartment());
            dto.setJoinDate(careerHistory.getJoinDate());
            dto.setRetireDate(careerHistory.getRetireDate());
            dto.setPosition(careerHistory.getPosition());
            dto.setSalary(careerHistory.getSalary());
            dto.setPositionSummary(careerHistory.getPositionSummary());
            dto.setExperience(careerHistory.getExperience());
            return dto;
        }
    }

    // 자격증 DTO
    @Data
    public static class CertificateDto {
        private Long id;
        private String certificateNm;
        private String obtainDate;
        private String agency;
        private String certificateNum;

        public static CertificateDto from(Certificate cert) {
            CertificateDto dto = new CertificateDto();
            dto.setId(cert.getSeqNoM112());
            dto.setCertificateNm(cert.getCertificateNm());
            dto.setObtainDate(cert.getObtainDate());
            dto.setAgency(cert.getAgency());
            dto.setCertificateNum(cert.getCertificateNum());
            return dto;
        }
    }

    // ✨ 복무증명서 첨부파일 DTO
    @Data
    public static class ServiceProofDto {
        private Long id;
        private String fileName;

        public static ServiceProofDto from(ServiceProofAttachment attachment) {
            ServiceProofDto dto = new ServiceProofDto();
            dto.setId(attachment.getSeqNoM114());
            dto.setFileName(attachment.getAttachFileNm());
            return dto;
        }
    }

    // ✨ 이력서 첨부파일 DTO
    @Data
    public static class ResumeFileDto {
        private Long id;
        private String fileName;

        public static ResumeFileDto from(ResumeFileAttachment attachment) {
            ResumeFileDto dto = new ResumeFileDto();
            dto.setId(attachment.getSeqNoM115());
            dto.setFileName(attachment.getAttachFileNm());
            return dto;
        }
    }
}