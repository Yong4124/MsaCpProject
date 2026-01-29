package com.example.personalJobs.repository;

import com.example.personalJobs.entity.ResumeFileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeFileAttachmentRepository extends JpaRepository<ResumeFileAttachment, Long> {

    Optional<ResumeFileAttachment>
    findTopByResume_SeqNoM110AndDelYnOrderBySeqNoM115Desc(Long seqNoM110, String delYn);

}
