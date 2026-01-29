package com.example.personalJobs.repository;

import com.example.personalJobs.entity.ServiceProofAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceProofAttachmentRepository extends JpaRepository<ServiceProofAttachment, Long> {

    Optional<ServiceProofAttachment> findTopByResume_SeqNoM110AndDelYnOrderBySeqNoM114Desc(Long seqNoM110, String delYn);
}
