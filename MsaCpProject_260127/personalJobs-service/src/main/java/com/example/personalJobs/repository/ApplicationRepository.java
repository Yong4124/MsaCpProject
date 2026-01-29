package com.example.personalJobs.repository;

import com.example.personalJobs.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findBySeqNoM210AndSeqNoM100AndDelYn(Long seqNoM210, Integer seqNoM100, String delYn);

    Optional<Application> findTopBySeqNoM210AndSeqNoM100AndDelYnOrderBySeqNoM300Desc(Long seqNoM210, Integer seqNoM100, String delYn);

    Page<Application> findBySeqNoM100AndDelYnOrderBySeqNoM300Desc(
            Integer seqNoM100, String delYn, Pageable pageable
    );

    // ApplyService에서 사용 (기존)
    Optional<Application> findTopBySeqNoM210AndSeqNoM100AndDelYnInOrderBySeqNoM300Desc(
            Long seqNoM210, Integer seqNoM100, List<String> delYnList
    );

    Page<Application> findBySeqNoM100AndDelYnInOrderBySeqNoM300Desc(
            Integer seqNoM100, List<String> delYnList, Pageable pageable
    );

    // ResumeController 등에서 사용
    List<Application> findBySeqNoM100AndDelYn(Integer seqNoM100, String delYn);

    // ✅ [필수] 이력서 ID(M110)로 제출된 내역이 있는지 조회 (ApplyService 중복 제거용)
    Optional<Application> findTopBySeqNoM110AndDelYnOrderBySeqNoM300Desc(Long seqNoM110, String delYn);
}