package com.example.personalJobs.service;

import com.example.personalJobs.dto.JobListItemResponse;
import com.example.personalJobs.dto.SearchField;
import com.example.personalJobs.entity.Job;
import com.example.personalJobs.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    public Page<JobListItemResponse> getJobs(
            Integer markOption,          // ✅ 파라미터는 유지(프론트 안 깨게) / 실제론 사용 안 함
            String groupCompany,         // ✅ 파라미터 유지 / 필요하면 COMPANY 검색으로 흡수
            SearchField searchField,
            String searchWord,
            String sortOption,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(size, 100),
                toSort(sortOption)
        );

        Page<Job> base;

        // ✅ 항상 del_yn='N' 기준
        if (searchField != null && StringUtils.hasText(searchWord)) {
            base = switch (searchField) {
                case TITLE -> jobRepository.findByTitleContainingIgnoreCaseAndDelYn(searchWord, "N", pageable);
                case COMPANY -> jobRepository.findByCompanyNameContainingIgnoreCaseAndDelYn(searchWord, "N", pageable);
                case LOCATION -> jobRepository.findByWorkLocationContainingIgnoreCaseAndDelYn(searchWord, "N", pageable);
            };
        } else {
            base = jobRepository.findByDelYn("N", pageable);
        }

        // ✅ groupCompany(그룹사) 필터를 따로 “Page에서 filter” 하려면 Page는 직접 filter가 안 됨
        //    가장 안전: groupCompany는 프론트에서 COMPANY 검색으로 보내게 하거나,
        //    지금은 그냥 무시(=일단 컴파일/동작 우선)
        //    (원하면 repo 메서드 하나 더 만들어서 groupCompany까지 DB에서 처리 가능)

        return base.map(this::toResponse);
    }

    public JobListItemResponse getJobDetail(Long id) {
        Job j = jobRepository.findById(id).orElseThrow();
        return toResponse(j);
    }

    private Sort toSort(String sortOption) {
        // 너 프론트 sortoption=START_DATE/END_DATE 같은 값 쓰는 것 같아서 간단히 매핑
        if ("END_DATE".equalsIgnoreCase(sortOption)) {
            return Sort.by(Sort.Direction.DESC, "endDate");
        }
        if ("START_DATE".equalsIgnoreCase(sortOption)) {
            return Sort.by(Sort.Direction.DESC, "startDate");
        }
        return Sort.by(Sort.Direction.DESC, "seqNoM210");
    }

    private JobListItemResponse toResponse(Job j) {
        return new JobListItemResponse(
                j.getSeqNoM210(),                 // id
                j.getTitle(),                     // title
                j.getCompanyName(),               // company
                j.getJobType(),                   // workType (너 HTML이 뭐 기대하는지에 따라 jobType/jobForm 바꿔도 됨)
                j.getJobForm(),                   // employmentType
                j.getJobCategory(),
                j.getIndustry(),
                j.getRoleLevel(),                 // level
                j.getExperience(),
                j.getBaseSalary(),                // salaryText
                j.getWorkTime(),                  // workingHours
                j.getWorkLocation(),              // location
                j.getLogoPath(),                  // logoUrl
                false,                            // marked (M210에는 없음 → 일단 false 고정)
                "Y".equalsIgnoreCase(j.getCloseYn()) // closed
        );
    }
}
