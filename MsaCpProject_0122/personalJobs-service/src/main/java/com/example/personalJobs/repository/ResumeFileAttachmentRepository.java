package com.example.personalJobs.repository;

import com.example.personalJobs.entity.ResumeFileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeFileAttachmentRepository extends JpaRepository<ResumeFileAttachment, Long> {
}
