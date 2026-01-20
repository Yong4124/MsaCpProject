package com.example.personalJobs.repository;

import com.example.personalJobs.entity.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    Optional<Resume> findTopBySeqNoM100AndDelYnOrderBySeqNoM110Desc(Long seqNoM100, String delYn);

    Page<Resume> findBySeqNoM100AndDelYnOrderBySeqNoM110Desc(Long seqNoM100, String delYn, Pageable pageable);

    Optional<Resume> findBySeqNoM110AndSeqNoM100AndDelYn(Long seqNoM110, Long seqNoM100, String delYn);
}
