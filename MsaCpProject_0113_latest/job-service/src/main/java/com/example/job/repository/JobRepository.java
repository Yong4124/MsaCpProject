// JobRepository.java
package com.example.job.repository;

import com.example.job.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
// JobRepository.java
public interface JobRepository extends JpaRepository<Job, Long> {

    // ✨ 변경: companyId로 조회
    List<Job> findByCompanyIdAndDelYnOrderBySeqNoM210Desc(Integer companyId, String delYn);

    // 공개 채용 목록
    List<Job> findByDelYnAndPostingYnOrderBySeqNoM210Desc(String delYn, String postingYn);

    // 삭제되지 않은 것만
    List<Job> findByDelYnOrderBySeqNoM210Desc(String delYn);

    // 상세 조회
    Optional<Job> findBySeqNoM210AndDelYn(Long seqNoM210, String delYn);
}