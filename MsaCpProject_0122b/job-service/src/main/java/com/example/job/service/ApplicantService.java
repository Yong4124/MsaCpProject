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
            // 전체 지원자 조회
            applicants = applicantRepository.findByJob_SeqNoM210(jobId);
        } else {
            // 상태별 지원자 조회
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

        // Lazy Loading 강제 초기화
        Resume resume = applicant.getResume();
        if (resume != null) {
            // ✨ 경력 이력 컬렉션 초기화
            if (resume.getCareerHistories() != null) {
                resume.getCareerHistories().size();
            }
            // 연관된 컬렉션을 명시적으로 로드
            if (resume.getCertificates() != null) {
                resume.getCertificates().size();
            }
            if (resume.getServiceProofAttachments() != null) {
                resume.getServiceProofAttachments().size();
            }
            if (resume.getResumeFileAttachments() != null) {
                resume.getResumeFileAttachments().size();
            }
        }

        return ApplicantDto.from(applicant);
    }
//
//    /**
//     * 채용공고 지원하기
//     */
//    @Transactional
//    public ApplicantDto saveApplicant(Long jobId, ApplicantDto dto) {
//        // 1. 필수 파라미터 검증
//        if (dto.getResumeId() == null) {
//            throw new IllegalArgumentException("지원에 필요한 이력서 ID가 누락되었습니다.");
//        }
//
//        // 2. 채용공고 존재 확인 및 마감 여부 확인
//        Job job = jobRepository.findBySeqNoM210AndDelYn(jobId, "N")
//                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다. ID: " + jobId));
//
//        // 마감된 공고인지 확인
//        if ("Y".equals(job.getCloseYn())) {
//            throw new IllegalArgumentException("마감된 채용공고입니다.");
//        }
//
//        // 3. 이력서 존재 확인
//        Resume resume = resumeRepository.findById(dto.getResumeId())
//                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다. ID: " + dto.getResumeId()));
//
//        // 4. 중복 지원 확인 (선택적)
//        List<Applicant> existingApplicants = applicantRepository.findByJob_SeqNoM210(jobId);
//        boolean alreadyApplied = existingApplicants.stream()
//                .anyMatch(a -> a.getResume() != null &&
//                        a.getResume().getSeqNoM110().equals(dto.getResumeId()));
//
//        if (alreadyApplied) {
//            throw new IllegalArgumentException("이미 해당 공고에 지원하셨습니다.");
//        }
//
//        // 5. 지원 정보 생성
//        Applicant applicant = new Applicant();
//        applicant.setJob(job);
//        applicant.setResume(resume);
//        applicant.setStatus("0");       // 0 = 미심사(기본값)
//        applicant.setCancelStatus("N"); // 취소 여부 N
//        applicant.setDelYn("N");        // 삭제 여부 N
//
//        // 6. 저장 및 DTO 변환 반환
//        Applicant saved = applicantRepository.save(applicant);
//        return ApplicantDto.from(saved);
//    }

    /**
     * 지원자 상태 변경 (심사 처리: 합격/불합격 등)
     * 상태 코드: 0=미심사, 1=서류합격, 2=최종합격, 3=불합격 (예시)
     */
    @Transactional
    public ApplicantDto updateStatus(Long applicantId, String status) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new IllegalArgumentException("지원자 정보를 찾을 수 없습니다. ID: " + applicantId));

        // 상태 검증 (선택적)
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("변경할 상태를 입력해주세요.");
        }

        applicant.setStatus(status);

        // 더티 체킹에 의해 자동 업데이트
        return ApplicantDto.from(applicant);
    }

    /**
     * 지원 취소 (지원자가 직접 취소)
     */
    @Transactional
    public void cancelApplication(Long applicantId) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new IllegalArgumentException("지원자 정보를 찾을 수 없습니다. ID: " + applicantId));

        applicant.setCancelStatus("Y");
    }

    /**
     * 지원 정보 삭제 (논리 삭제)
     */
    @Transactional
    public void deleteApplicant(Long applicantId) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new IllegalArgumentException("지원자 정보를 찾을 수 없습니다. ID: " + applicantId));

        applicant.setDelYn("Y");
    }
}