package com.example.job.repository;

import com.example.job.model.ResumeFileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeFileAttachmentRepository extends JpaRepository<ResumeFileAttachment, Long> {
}
