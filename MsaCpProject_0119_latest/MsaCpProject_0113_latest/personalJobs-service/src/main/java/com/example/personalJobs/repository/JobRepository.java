package com.example.personalJobs.repository;

import com.example.personalJobs.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {

    // 기본: del_yn='N'만
    Page<Job> findByDelYn(String delYn, Pageable pageable);

    Page<Job> findByTitleContainingIgnoreCaseAndDelYn(String title, String delYn, Pageable pageable);

    Page<Job> findByCompanyNameContainingIgnoreCaseAndDelYn(String companyName, String delYn, Pageable pageable);

    Page<Job> findByWorkLocationContainingIgnoreCaseAndDelYn(String workLocation, String delYn, Pageable pageable);
}
