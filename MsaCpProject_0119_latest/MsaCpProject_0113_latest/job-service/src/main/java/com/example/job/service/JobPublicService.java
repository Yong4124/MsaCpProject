package com.example.job.service;

import com.example.job.dto.JobDto;
import com.example.job.model.Job;
import com.example.job.repository.CompanyMemberRepository;
import com.example.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobPublicService {

    private final JobRepository jobRepository;
    private final CompanyMemberRepository companyRepository; // 추가

    /**
     * 공개된 채용공고 목록 조회 (회사 정보 포함)
     */
    public List<JobDto> getPublicJobList() {
        return jobRepository
                .findByDelYnOrderBySeqNoM210Desc("N")
                .stream()
                .map(job -> {
                    JobDto dto = JobDto.from(job);
                    enrichCompanyInfo(dto, job.getCompanyId()); // 회사 정보 추가
                    return dto;
                })
                .toList();
    }

    /**
     * 공개된 채용공고 상세 조회 (회사 정보 포함)
     */
    public JobDto getPublicJobDetail(Long id) {
        Job job = jobRepository
                .findBySeqNoM210AndDelYn(id, "N")
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다. ID: " + id));

        JobDto dto = JobDto.from(job);
        enrichCompanyInfo(dto, job.getCompanyId()); // 회사 정보 추가
        return dto;
    }

    /**
     * 회사 정보를 T_JB_M200에서 가져와서 DTO에 설정
     */
    private void enrichCompanyInfo(JobDto dto, Integer companyId) {
        if (companyId == null) {
            setDefaultCompanyInfo(dto);
            return;
        }

        companyRepository.findById(companyId).ifPresentOrElse(
                company -> {
                    // T_JB_M200에서 가져온 회사 정보 설정
                    dto.setCompanyName(company.getCompanyName());
                    dto.setLogoPath(company.getLogoPath());
                    dto.setCeoName(company.getCeoName());
                    dto.setCompanyAddress(company.getCompanyAddress());

                    // ✅ 추가: 기업전경(photoPath)도 채움
                    // (채용공고 자체에 photoPath가 이미 있으면 그 값 우선)
                    if (dto.getPhotoPath() == null || dto.getPhotoPath().trim().isEmpty()) {
                        dto.setPhotoPath(company.getPhotoPath());
                    }
                },
                () -> setDefaultCompanyInfo(dto)
        );
    }

    /**
     * 기본 회사 정보 설정
     */
    private void setDefaultCompanyInfo(JobDto dto) {
        if (dto.getCompanyName() == null || dto.getCompanyName().trim().isEmpty()) {
            dto.setCompanyName("회사명 미등록");
        }
        if (dto.getLogoPath() == null || dto.getLogoPath().trim().isEmpty()) {
            dto.setLogoPath("/img/common/default_logo.png");
        }
        if (dto.getCeoName() == null || dto.getCeoName().trim().isEmpty()) {
            dto.setCeoName("대표자명 미등록");
        }
        if (dto.getCompanyAddress() == null || dto.getCompanyAddress().trim().isEmpty()) {
            dto.setCompanyAddress("주소 미등록");
        }
        // (photoPath 기본값은 프론트에서 이미 처리하니까 굳이 안 넣어도 됨)
    }
}
