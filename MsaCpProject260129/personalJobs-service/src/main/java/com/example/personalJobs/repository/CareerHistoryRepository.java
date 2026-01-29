package com.example.personalJobs.repository;

import com.example.personalJobs.entity.CareerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CareerHistoryRepository extends JpaRepository<CareerHistory, Long> {

    // ✅ 이력서(SEQ_NO_M110) 기준 경력 전체 조회 (여러 건 나와야 함)
    List<CareerHistory> findByResume_SeqNoM110AndDelYnOrderBySeqNoM111Asc(Long seqNoM110, String delYn);

    // ✅ 단건 조회(수정/삭제용)
    Optional<CareerHistory> findBySeqNoM111AndDelYn(Long seqNoM111, String delYn);

    // ✅ 이력서 기준 전체 소프트삭제(교체 저장/초기화/삭제 시 유용)
    @Modifying
    @Query("""
        update CareerHistory c
           set c.delYn = 'Y'
         where c.resume.seqNoM110 = :seqNoM110
           and c.delYn = 'N'
    """)
    int softDeleteAllByResume(@Param("seqNoM110") Long seqNoM110);

}
