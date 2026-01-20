package com.example.personalJobs.repository;

import com.example.personalJobs.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findBySeqNoM210AndSeqNoM100AndDelYn(Long seqNoM210, Integer seqNoM100, String delYn);

    Optional<Application> findTopBySeqNoM210AndSeqNoM100AndDelYnOrderBySeqNoM300Desc(Long seqNoM210, Integer seqNoM100, String delYn);
}
