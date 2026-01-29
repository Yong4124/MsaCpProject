package com.example.job.controller;

import com.example.job.model.ResumeFileAttachment;
import com.example.job.model.ServiceProofAttachment;
import com.example.job.repository.ResumeFileAttachmentRepository;
import com.example.job.repository.ServiceProofAttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileDownloadController {

    private final ServiceProofAttachmentRepository serviceProofAttachmentRepository;
    private final ResumeFileAttachmentRepository resumeFileAttachmentRepository;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    /**
     * 복무증명서 파일 다운로드
     */
    @GetMapping("/service-proof/{id}/download")
    public ResponseEntity<Resource> downloadServiceProof(
            @PathVariable Long id,
            @CookieValue(value = "JWT_TOKEN", required = false) String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ServiceProofAttachment attachment = serviceProofAttachmentRepository.findById(id)
                .orElse(null);

        if (attachment == null || attachment.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        return downloadFile(attachment.getFilePath(), attachment.getAttachFileNm());
    }

    /**
     * 이력서 첨부파일 다운로드
     */
    @GetMapping("/resume-file/{id}/download")
    public ResponseEntity<Resource> downloadResumeFile(
            @PathVariable Long id,
            @CookieValue(value = "JWT_TOKEN", required = false) String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResumeFileAttachment attachment = resumeFileAttachmentRepository.findById(id)
                .orElse(null);

        if (attachment == null || attachment.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        return downloadFile(attachment.getFilePath(), attachment.getAttachFileNm());
    }

    /**
     * 공통 파일 다운로드 처리
     */
    private ResponseEntity<Resource> downloadFile(String filePath, String originalFileName) {
        try {
            // /uploads/xxx/yyy.pdf -> ./uploads/xxx/yyy.pdf
            String cleanPath = filePath.startsWith("/") ? "." + filePath : filePath;
            Path path = Paths.get(cleanPath).normalize();

            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                    .body(resource);

        } catch (Exception e) {
            System.err.println("파일 다운로드 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
