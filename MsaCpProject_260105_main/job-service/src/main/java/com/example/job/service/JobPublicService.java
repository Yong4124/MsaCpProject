package com.example.job.service;

import com.example.job.dto.JobDto;
import com.example.job.model.Job;
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

    /**
     * 공개된 채용공고 목록 조회
     */
    public List<JobDto> getPublicJobList() {
        // postingYn 체크 없이 삭제되지 않은 모든 공고 조회
        // 마감 여부는 프론트엔드에서 closeYn과 endDate로 판단
        return jobRepository
                .findByDelYnOrderBySeqNoM210Desc("N")
                .stream()
                .map(JobDto::from)
                .toList();
    }

    /**
     * 공개된 채용공고 상세 조회
     */
    public JobDto getPublicJobDetail(Long id) {
        Job job = jobRepository
                .findBySeqNoM210AndDelYn(id, "N")
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다. ID: " + id));
        return JobDto.from(job);
    }
}