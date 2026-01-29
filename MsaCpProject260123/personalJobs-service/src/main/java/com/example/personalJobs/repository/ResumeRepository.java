package com.example.personalJobs.repository;

import com.example.personalJobs.entity.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // 최신 이력서 1개 조회 (임시저장 불러오기용)
    Optional<Resume> findTopBySeqNoM100AndDelYnOrderBySeqNoM110Desc(Long seqNoM100, String delYn);

    // 내 이력서 전체 목록 조회 (팝업/리스트용)
    Page<Resume> findBySeqNoM100AndDelYnOrderBySeqNoM110Desc(Long seqNoM100, String delYn, Pageable pageable);

    // 특정 이력서 조회
    Optional<Resume> findBySeqNoM110AndSeqNoM100AndDelYn(Long seqNoM110, Long seqNoM100, String delYn);

    // 🚨 [필수 추가] 강제 업데이트 쿼리 (이게 있어야 공고 ID가 무조건 저장됨)
    @Modifying
    @Query("UPDATE Resume r SET r.task = :task WHERE r.seqNoM110 = :id")
    void updateTask(@Param("id") Long id, @Param("task") String task);
}