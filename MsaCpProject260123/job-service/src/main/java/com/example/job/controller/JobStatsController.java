package com.example.job.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 관리자 통계 API 컨트롤러
 *
 * Base Path: /api/jobs/admin/stats
 *
 * - 채용공고(월별):   GET /recruit-monthly
 * - 공고명별(리스트): GET /job-name
 * - 회사별공고:       GET /company
 * - 회사별공고(월별): GET /company-monthly
 */
@RestController
@RequestMapping("/api/jobs/admin/stats")
@RequiredArgsConstructor
public class JobStatsController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 채용공고 월별 통계 조회
     *
     * 호출 예)
     * GET /api/jobs/admin/stats/recruit-monthly?from=2026-01-01&to=2026-12-31
     *
     * 응답 구조(프론트 기대)
     * {
     *   "total": 12,
     *   "totalApplicants": 34,
     *   "rows": [
     *     { "ym": "2026-01", "jobCount": 5, "applicantCount": 10 },
     *     ...
     *   ]
     * }
     */
    @GetMapping("/recruit-monthly")
    public Map<String, Object> getRecruitMonthly(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        // 월별(YYYY-MM) 기준으로 공고수/지원자수 집계
        StringBuilder sql = new StringBuilder("""
            SELECT
                DATE_FORMAT(m210.start_date, '%Y-%m') AS ym,
                COUNT(DISTINCT m210.seq_no_m210)      AS jobCount,
                COUNT(m300.seq_no_m300)               AS applicantCount
            FROM t_jb_m210 m210
            LEFT JOIN t_jb_m300 m300
                ON m210.seq_no_m210 = m300.job_id
                AND m300.del_yn = 'N'
            WHERE IFNULL(m210.del_yn,'N') = 'N'
              AND IFNULL(m210.posting_yn, '1') = '1'
        """);

        List<Object> params = new ArrayList<>();

        // 시작일 from 조건 (선택)
        if (from != null) {
            sql.append(" AND m210.start_date >= ?");
            params.add(Date.valueOf(from));
        }

        // 종료일 to 조건 (선택)
        // - to를 포함하려면 "to + 1일 미만"이 가장 안전한 패턴(시간값 존재해도 포함됨)
        if (to != null) {
            sql.append(" AND m210.start_date < ?");
            params.add(Date.valueOf(to.plusDays(1)));
        }

        sql.append(" GROUP BY DATE_FORMAT(m210.start_date, '%Y-%m') ORDER BY ym DESC");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());

        int totalJobs = rows.stream()
                .mapToInt(r -> ((Number) r.get("jobCount")).intValue())
                .sum();

        int totalApplicants = rows.stream()
                .mapToInt(r -> ((Number) r.get("applicantCount")).intValue())
                .sum();

        return Map.of(
                "total", totalJobs,
                "totalApplicants", totalApplicants,
                "rows", rows
        );
    }

    // 공고명별 통계 (공고 단위 리스트)
    @GetMapping("/job-name")
    public Map<String, Object> getJobNameStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @RequestParam(required = false) String job,      // 공고명 검색(부분)
            @RequestParam(required = false) String company   // 회사명 검색(부분)
    ) {
        StringBuilder sql = new StringBuilder("""
        SELECT
            m210.seq_no_m210 AS jobSeq,
            m210.title       AS jobName,

            -- ✅ [그룹]에 들어갈 값: parent_company_cd (SK / SAMSUNG / SOULBRAIN ...)
            IFNULL(c.parent_company_cd, '') AS groupName,

            -- ✅ 회사명: 회사 마스터 우선, 없으면 공고 테이블 값
            IFNULL(c.company, m210.company) AS companyName,

            -- ✅ varchar 날짜 안전 변환/표시
            DATE_FORMAT(STR_TO_DATE(LEFT(m210.start_date, 10), '%Y-%m-%d'), '%Y-%m-%d') AS startDate,
            DATE_FORMAT(STR_TO_DATE(LEFT(m210.end_date,   10), '%Y-%m-%d'), '%Y-%m-%d') AS endDate,

            1 AS jobCount,
            COUNT(m300.seq_no_m300) AS applicantCount
        FROM t_jb_m210 m210

        LEFT JOIN t_jb_m200 c
            ON c.seq_no_m200 = m210.seq_no_m200
            AND IFNULL(c.del_yn,'N') = 'N'

        LEFT JOIN t_jb_m300 m300
            ON m210.seq_no_m210 = m300.job_id
            AND IFNULL(m300.del_yn,'N') = 'N'

        WHERE IFNULL(m210.del_yn,'N') = 'N'
          AND IFNULL(m210.posting_yn, '1') = '1'
    """);

        List<Object> params = new ArrayList<>();

        // ✅ 기간 필터(선택): start_date 기준
        if (from != null) {
            sql.append(" AND STR_TO_DATE(LEFT(m210.start_date, 10), '%Y-%m-%d') >= ?");
            params.add(Date.valueOf(from));
        }
        if (to != null) {
            sql.append(" AND STR_TO_DATE(LEFT(m210.start_date, 10), '%Y-%m-%d') < ?");
            params.add(Date.valueOf(to.plusDays(1)));
        }

        // ✅ 공고명 검색(선택)
        if (job != null && !job.isBlank()) {
            sql.append(" AND m210.title LIKE ?");
            params.add("%" + job.trim() + "%");
        }

        // ✅ 회사명 검색(선택)
        if (company != null && !company.isBlank()) {
            sql.append(" AND IFNULL(c.company, m210.company) LIKE ?");
            params.add("%" + company.trim() + "%");
        }

        sql.append("""
        GROUP BY
            m210.seq_no_m210, m210.title,
            c.parent_company_cd, c.company, m210.company,
            m210.start_date, m210.end_date
        ORDER BY
            STR_TO_DATE(LEFT(m210.start_date, 10), '%Y-%m-%d') DESC,
            STR_TO_DATE(LEFT(m210.end_date,   10), '%Y-%m-%d') DESC
    """);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());

        int totalJobs = rows.size();
        int totalApplicants = rows.stream()
                .mapToInt(r -> ((Number) r.get("applicantCount")).intValue())
                .sum();

        return Map.of(
                "total", totalJobs,
                "totalApplicants", totalApplicants,
                "rows", rows
        );
    }

    // 회사별공고 통계
    // GET /api/jobs/admin/stats/company?from=2026-01-01&to=2026-12-31&company=SK
    @GetMapping("/company")
    public Map<String, Object> getCompanyStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @RequestParam(required = false) String company
    ) {
        StringBuilder sql = new StringBuilder("""
        SELECT
            IFNULL(c.company, m210.company)        AS companyName,
            IFNULL(c.parent_company_cd, '')       AS groupName,
            COUNT(DISTINCT m210.seq_no_m210)      AS jobCount,
            COUNT(m300.seq_no_m300)               AS applicantCount
        FROM t_jb_m210 m210
        LEFT JOIN t_jb_m200 c
            ON c.seq_no_m200 = m210.seq_no_m200
            AND IFNULL(c.del_yn,'N') = 'N'
        LEFT JOIN t_jb_m300 m300
            ON m300.job_id = m210.seq_no_m210
            AND IFNULL(m300.del_yn,'N') = 'N'
        WHERE IFNULL(m210.del_yn,'N') = 'N'
          AND IFNULL(m210.posting_yn, '1') = '1'
    """);

        List<Object> params = new ArrayList<>();

        // ✅ 기간 필터(start_date 기준)
        if (from != null) {
            sql.append(" AND STR_TO_DATE(LEFT(m210.start_date, 10), '%Y-%m-%d') >= ?");
            params.add(Date.valueOf(from));
        }
        if (to != null) {
            sql.append(" AND STR_TO_DATE(LEFT(m210.start_date, 10), '%Y-%m-%d') < ?");
            params.add(Date.valueOf(to.plusDays(1)));
        }

        // ✅ 회사명 검색
        if (company != null && !company.isBlank()) {
            sql.append(" AND IFNULL(c.company, m210.company) LIKE ?");
            params.add("%" + company.trim() + "%");
        }

        sql.append("""
        GROUP BY IFNULL(c.company, m210.company), IFNULL(c.parent_company_cd, '')
        ORDER BY jobCount DESC, applicantCount DESC, companyName ASC
    """);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());

        int totalJobs = rows.stream()
                .mapToInt(r -> ((Number) r.get("jobCount")).intValue())
                .sum();

        int totalApplicants = rows.stream()
                .mapToInt(r -> ((Number) r.get("applicantCount")).intValue())
                .sum();

        return Map.of(
                "total", totalJobs,
                "totalApplicants", totalApplicants,
                "rows", rows
        );
    }

    // 회사별공고(월별) 통계
    // GET /api/jobs/admin/stats/company-monthly?from=2026-01-01&to=2026-12-31&company=SK
    @GetMapping("/company-monthly")
    public Map<String, Object> getCompanyMonthlyStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @RequestParam(required = false) String company
    ) {
        StringBuilder sql = new StringBuilder("""
        SELECT
            DATE_FORMAT(STR_TO_DATE(LEFT(m210.start_date, 10), '%Y-%m-%d'), '%Y-%m') AS ym,
            IFNULL(c.company, m210.company)  AS companyName,
            IFNULL(c.parent_company_cd, '') AS groupName,
            COUNT(DISTINCT m210.seq_no_m210) AS jobCount,
            COUNT(m300.seq_no_m300)          AS applicantCount
        FROM t_jb_m210 m210
        LEFT JOIN t_jb_m200 c
            ON c.seq_no_m200 = m210.seq_no_m200
            AND IFNULL(c.del_yn,'N') = 'N'
        LEFT JOIN t_jb_m300 m300
            ON m300.job_id = m210.seq_no_m210
            AND IFNULL(m300.del_yn,'N') = 'N'
        WHERE IFNULL(m210.del_yn,'N') = 'N'
          AND IFNULL(m210.posting_yn, '1') = '1'
          AND m210.start_date IS NOT NULL
          AND m210.start_date <> ''
    """);

        List<Object> params = new ArrayList<>();

        // ✅ 기간 필터(start_date 기준)
        if (from != null) {
            sql.append(" AND STR_TO_DATE(LEFT(m210.start_date, 10), '%Y-%m-%d') >= ?");
            params.add(Date.valueOf(from));
        }
        if (to != null) {
            sql.append(" AND STR_TO_DATE(LEFT(m210.start_date, 10), '%Y-%m-%d') < ?");
            params.add(Date.valueOf(to.plusDays(1)));
        }

        // ✅ 회사명 검색
        if (company != null && !company.isBlank()) {
            sql.append(" AND IFNULL(c.company, m210.company) LIKE ?");
            params.add("%" + company.trim() + "%");
        }

        sql.append("""
        GROUP BY
            DATE_FORMAT(STR_TO_DATE(LEFT(m210.start_date, 10), '%Y-%m-%d'), '%Y-%m'),
            IFNULL(c.company, m210.company),
            IFNULL(c.parent_company_cd, '')
        ORDER BY ym DESC, jobCount DESC, applicantCount DESC, companyName ASC
    """);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());

        int totalJobs = rows.stream()
                .mapToInt(r -> ((Number) r.get("jobCount")).intValue())
                .sum();

        int totalApplicants = rows.stream()
                .mapToInt(r -> ((Number) r.get("applicantCount")).intValue())
                .sum();

        return Map.of(
                "total", totalJobs,
                "totalApplicants", totalApplicants,
                "rows", rows
        );
    }
}
