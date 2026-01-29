package com.example.job.repository;

import com.example.job.model.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    // 전체 지원자
    List<Applicant> findByJob_SeqNoM210(Long jobId);

    // 상태 필터
    List<Applicant> findByJob_SeqNoM210AndStatus(Long jobId, String status);
}

