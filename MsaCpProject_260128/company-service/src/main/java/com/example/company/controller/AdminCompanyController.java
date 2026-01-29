package com.example.company.controller;

import com.example.company.dto.CompanyDetailResponse;
import com.example.company.model.ApprovalYn;
import com.example.company.model.Company;
import com.example.company.model.DelYn;
import com.example.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/company/admin")
@RequiredArgsConstructor
public class AdminCompanyController {

    private final CompanyRepository companyRepository;

    // ✅ 기업회원 목록 + 검색(통합)
    // GET /api/company/admin/companies
    // - 전체: /api/company/admin/companies
    // - 승인필터: /api/company/admin/companies?approvalYn=Y|N
    // - 검색: /api/company/admin/companies?field=company&keyword=abc
    // - 조합: /api/company/admin/companies?approvalYn=N&field=all&keyword=test
    @GetMapping("/companies")
    public ResponseEntity<List<Map<String, Object>>> list(
            @RequestParam(value = "field", required = false, defaultValue = "all") String field,
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "approvalYn", required = false, defaultValue = "all") String approvalYn
    ) {
        // approvalYn: all / Y / N -> ApprovalYn 타입으로 변환 (all이면 null)
        ApprovalYn approval = null;
        if ("Y".equalsIgnoreCase(approvalYn)) approval = ApprovalYn.Y;
        if ("N".equalsIgnoreCase(approvalYn)) approval = ApprovalYn.N;

        // ✅ repository의 searchForAdmin 호출
        List<Company> companies = companyRepository.searchForAdmin(
                DelYn.N,
                approval,
                field,
                keyword == null ? "" : keyword.trim()
        );

        List<Map<String, Object>> result = companies.stream()
                .map(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("no", c.getSeqNoM200());
                    m.put("company", c.getCompany());
                    m.put("managerNm", c.getManagerNm());
                    m.put("loginId", c.getLoginId());   // ✅ 키 통일 추천
                    m.put("insertDate", c.getInsertDate() != null ? c.getInsertDate().toString() : "");
                    m.put("approvalYn", c.getApprovalYn() != null ? c.getApprovalYn().name() : "N");
                    return m;
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    // ✅ 기업회원 상세
    // GET /api/company/admin/companies/{seq}
    @GetMapping("/companies/{seq}")
    public ResponseEntity<CompanyDetailResponse> detail(@PathVariable Integer seq) {
        Company c = companyRepository.findById(seq)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(CompanyDetailResponse.from(c));
    }

    // ✅ 기업회원 승인/미승인 변경
    // PUT /api/company/admin/companies/{seq}/approval
    // body: { "approvalYn": "Y" } or { "approvalYn": "N" }
    @PutMapping("/companies/{seq}/approval")
    public ResponseEntity<Void> updateApproval(
            @PathVariable Integer seq,
            @RequestBody Map<String, String> body
    ) {
        String approvalYn = body.get("approvalYn");
        if (!"Y".equalsIgnoreCase(approvalYn) && !"N".equalsIgnoreCase(approvalYn)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "approvalYn must be Y or N");
        }

        Company c = companyRepository.findById(seq)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        c.setApprovalYn("Y".equalsIgnoreCase(approvalYn) ? ApprovalYn.Y : ApprovalYn.N);
        companyRepository.save(c);

        return ResponseEntity.ok().build();
    }
    // AdminCompanyController.java
// ✅ 기업회원 월별 가입 통계
// GET /api/company/admin/stats/join-monthly?from=2025-01-01&to=2025-12-31
    @GetMapping("/stats/join-monthly")
    public ResponseEntity<Map<String, Object>> joinMonthly(
            @RequestParam("from") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate from,
            @RequestParam("to") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate to
    ) {
        long total = companyRepository.countTotalJoins(DelYn.N, from, to);

        List<Map<String, Object>> rows = companyRepository.countMonthlyJoins(DelYn.N, from, to).stream()
                .map(r -> {
                    String ym = String.format("%04d-%02d", r.getY(), r.getM());
                    Map<String, Object> m = new HashMap<>();
                    m.put("ym", ym);
                    m.put("count", r.getCnt());
                    return m;
                })
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("rows", rows);
        return ResponseEntity.ok(result);
    }
}
