package com.example.job.service;

import com.example.job.dto.ApplicantDto;
import com.example.job.model.Applicant;
import com.example.job.model.Job;
import com.example.job.model.Resume;
import com.example.job.repository.ApplicantRepository;
import com.example.job.repository.JobRepository;
import com.example.job.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicantService {

    private final ApplicantRepository applicantRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;

    /**
     * 특정 채용공고의 지원자 목록 조회 (상태 필터 가능)
     */
    public List<ApplicantDto> getApplicants(Long jobId, String status) {
        List<Applicant> applicants;

        if (status == null || status.isBlank()) {
            applicants = applicantRepository.findByJob_SeqNoM210(jobId);
        } else {
            applicants = applicantRepository.findByJob_SeqNoM210AndStatus(jobId, status);
        }

        return applicants.stream()
                .map(ApplicantDto::from)
                .toList();
    }

    /**
     * 지원자 상세 조회
     */
    public ApplicantDto getApplicant(Long id) {
        Applicant applicant = applicantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("지원자 정보를 찾을 수 없습니다. ID: " + id));
        return ApplicantDto.from(applicant);
    }

    /**
     * 채용공고 지원하기
     */
    @Transactional
    public ApplicantDto saveApplicant(Long jobId, ApplicantDto dto) {
        // 1. 필수 파라미터 검증
        if (dto.getResumeId() == null) {
            throw new IllegalArgumentException("지원에 필요한 이력서 ID가 누락되었습니다.");
        }

        // 2. 채용공고 존재 확인
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다. ID: " + jobId));

        // 3. 이력서 존재 확인 (확장된 필드들이 포함된 Resume 엔티티를 가져옴)
        Resume resume = resumeRepository.findById(dto.getResumeId())
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다. ID: " + dto.getResumeId()));

        // 4. 지원 정보 생성
        Applicant applicant = new Applicant();
        applicant.setJob(job);
        applicant.setResume(resume); // 확장된 필드가 있는 resume 객체 연결
        applicant.setStatus("0");    // 0 = 미심사(기본값)

        // 5. 저장 및 DTO 변환 반환
        Applicant saved = applicantRepository.save(applicant);
        return ApplicantDto.from(saved);
    }

    /**
     * 지원자 상태 변경 (심사 처리: 합격/불합격 등)
     */
    @Transactional
    public ApplicantDto updateStatus(Long applicantId, String status) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new IllegalArgumentException("지원자 정보를 찾을 수 없습니다. ID: " + applicantId));

        applicant.setStatus(status);
        // 더티 체킹에 의해 자동 업데이트되지만, 명시적으로 저장 후 DTO 반환 가능
        return ApplicantDto.from(applicant);
    }
}