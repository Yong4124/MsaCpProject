package com.example.personalJobs.repository;

import com.example.personalJobs.entity.ResumeAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.nio.file.spi.FileTypeDetector;
import java.util.Optional;

public interface ResumeAttachmentRepository
        extends JpaRepository<ResumeAttachment, Long> {

    Optional<ResumeAttachment>
    findTopByResume_SeqNoM110AndFileTypeAndDelYnOrderBySeqNoM113Desc(
            Long seqNoM110,
            String fileType,
            String delYn
    );
}
