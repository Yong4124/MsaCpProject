package com.example.personalJobs.repository;

import com.example.personalJobs.entity.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // ✅ 추가됨
import org.springframework.data.jpa.repository.Query;     // ✅ 추가됨
import org.springframework.data.repository.query.Param;   // ✅ 추가됨

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // 최신 이력서 1개 조회 (임시저장 불러오기용)
    Optional<Resume> findTopBySeqNoM100AndDelYnOrderBySeqNoM110Desc(Long seqNoM100, String delYn);

    // 내 이력서 전체 목록 조회 (팝업/리스트용)
    Page<Resume> findBySeqNoM100AndDelYnOrderBySeqNoM110Desc(Long seqNoM100, String delYn, Pageable pageable);

    // 특정 이력서 조회
    Optional<Resume> findBySeqNoM110AndSeqNoM100AndDelYn(Long seqNoM110, Long seqNoM100, String delYn);

}