package com.example.job.dto;

import com.example.job.model.Applicant;
import com.example.job.model.Resume;
import com.example.job.model.Certificate;
import com.example.job.model.ServiceProofAttachment;
import com.example.job.model.ResumeFileAttachment;
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

    // ✨ 경력
    private String company;
    private String dept;
    private String joinDate;
    private String leaveDate;
    private String position;
    private String salary;
    private String task;
    private String careerDesc;

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

        // 채용공고 ID 세팅
        if (applicant.getJob() != null) {
            dto.setJobId(applicant.getJob().getSeqNoM210());
        }

        dto.setStatus(applicant.getStatus());

        Resume resume = applicant.getResume();
        if (resume != null) {
            dto.setResumeId(resume.getSeqNoM110());

            // [인적사항]
            dto.setName(resume.getName());
            dto.setGender(resume.getGender());
            dto.setBirthDate(resume.getBirthDate());
            dto.setPhone(resume.getPhone());
            dto.setEmail(resume.getEmail());
            dto.setAddress(resume.getAddress());

            // [최종학력]
            dto.setSchoolName(resume.getSchoolName());
            dto.setMajor(resume.getMajor());
            dto.setEntranceDate(resume.getEntranceDate());
            dto.setGradDate(resume.getGradDate());
            dto.setScore(resume.getScore());
            dto.setGradStatus(resume.getGradStatus());

            // ✨ [경력]
            dto.setCompany(resume.getCompany());
            dto.setDept(resume.getDept());
            dto.setJoinDate(resume.getJoinDate());
            dto.setLeaveDate(resume.getLeaveDate());
            dto.setPosition(resume.getPosition());
            dto.setSalary(resume.getSalary());
            dto.setTask(resume.getTask());
            dto.setCareerDesc(resume.getCareerDesc());

            // [전문분야]
            dto.setSpeciality(resume.getSpeciality());

            // [자기소개]
            dto.setIntroduction(resume.getIntroduction());

            // [자격증]
            if (resume.getCertificates() != null) {
                dto.setCertificates(
                        resume.getCertificates().stream()
                                .map(CertificateDto::from)
                                .collect(Collectors.toList())
                );
            }

            // ✨ [복무증명서 첨부파일]
            if (resume.getServiceProofAttachments() != null) {
                dto.setServiceProofFiles(
                        resume.getServiceProofAttachments().stream()
                                .map(ServiceProofDto::from)
                                .collect(Collectors.toList())
                );
            }

            // ✨ [이력서 첨부파일]
            if (resume.getResumeFileAttachments() != null) {
                dto.setResumeFiles(
                        resume.getResumeFileAttachments().stream()
                                .map(ResumeFileDto::from)
                                .collect(Collectors.toList())
                );
            }
        }

        return dto;
    }

    // 자격증 DTO
    @Data
    public static class CertificateDto {
        private String certificateNm;
        private String obtainDate;
        private String agency;
        private String certificateNum;

        public static CertificateDto from(Certificate cert) {
            CertificateDto dto = new CertificateDto();
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
