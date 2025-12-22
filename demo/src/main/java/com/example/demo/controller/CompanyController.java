package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyMemberService companyMemberService;
    private final JobPostingService jobPostingService;
    private final ApplicationService applicationService;

    // 현재 로그인한 기업회원 조회
    private CompanyMember getCurrentMember(Authentication authentication) {
        String loginId = authentication.getName();
        return companyMemberService.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

    // 마이페이지 메인
    @GetMapping("/mypage")
    public String mypage(Authentication authentication, Model model) {
        CompanyMember member = getCurrentMember(authentication);
        List<JobPosting> jobPostings = jobPostingService.findByCompanyMemberId(member.getId());
        
        // 총 지원자 수 계산
        int totalApplicants = 0;
        for (JobPosting job : jobPostings) {
            totalApplicants += applicationService.findByJobPostingId(job.getId()).size();
        }
        
        model.addAttribute("member", member);
        model.addAttribute("jobPostings", jobPostings);
        model.addAttribute("totalApplicants", totalApplicants);
        
        return "company/mypage";
    }

    // 회원정보 수정 폼
    @GetMapping("/edit")
    public String editForm(Authentication authentication, Model model) {
        CompanyMember member = getCurrentMember(authentication);
        model.addAttribute("member", member);
        return "company/edit";
    }

    // 회원정보 수정 처리
    @PostMapping("/edit")
    public String edit(Authentication authentication,
                       @ModelAttribute CompanyMember updatedMember,
                       RedirectAttributes redirectAttributes) {
        CompanyMember member = getCurrentMember(authentication);
        
        member.setManagerName(updatedMember.getManagerName());
        member.setDepartment(updatedMember.getDepartment());
        member.setPhone(updatedMember.getPhone());
        member.setEmail(updatedMember.getEmail());
        member.setCompany(updatedMember.getCompany());
        member.setPresidentName(updatedMember.getPresidentName());
        member.setCompanyAddress(updatedMember.getCompanyAddress());
        member.setParentCompanyCd(updatedMember.getParentCompanyCd());
        
        companyMemberService.update(member);
        
        redirectAttributes.addFlashAttribute("successMessage", "회원정보가 수정되었습니다.");
        return "redirect:/company/mypage";
    }

    // 채용공고 관리
    @GetMapping("/jobs")
    public String jobs(Authentication authentication, Model model) {
        CompanyMember member = getCurrentMember(authentication);
        List<JobPosting> jobPostings = jobPostingService.findByCompanyMemberId(member.getId());
        
        model.addAttribute("jobPostings", jobPostings);
        return "company/jobs";
    }

    // 채용공고 등록 폼
    @GetMapping("/jobs/new")
    public String newJobForm(Authentication authentication, Model model) {
        CompanyMember member = getCurrentMember(authentication);
        model.addAttribute("member", member);
        model.addAttribute("job", new JobPosting());
        return "company/job-form";
    }

    // 채용공고 등록 처리
    @PostMapping("/jobs/new")
    public String createJob(Authentication authentication,
                            @ModelAttribute JobPosting jobPosting,
                            @RequestParam(required = false) String action,
                            RedirectAttributes redirectAttributes) {
        CompanyMember member = getCurrentMember(authentication);
        jobPosting.setCompanyMember(member);
        
        if ("temp".equals(action)) {
            jobPostingService.saveTemp(jobPosting);
            redirectAttributes.addFlashAttribute("successMessage", "임시저장되었습니다.");
        } else {
            jobPostingService.publish(jobPosting);
            redirectAttributes.addFlashAttribute("successMessage", "채용공고가 등록되었습니다.");
        }
        
        return "redirect:/company/jobs";
    }

    // 채용공고 수정 폼
    @GetMapping("/jobs/{id}/edit")
    public String editJobForm(@PathVariable Long id, Authentication authentication, Model model) {
        CompanyMember member = getCurrentMember(authentication);
        JobPosting job = jobPostingService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다."));
        
        // 본인 공고인지 확인
        if (!job.getCompanyMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
        
        model.addAttribute("member", member);
        model.addAttribute("job", job);
        return "company/job-form";
    }

    // 채용공고 수정 처리
    @PostMapping("/jobs/{id}/edit")
    public String updateJob(@PathVariable Long id,
                            Authentication authentication,
                            @ModelAttribute JobPosting updatedJob,
                            @RequestParam(required = false) String action,
                            RedirectAttributes redirectAttributes) {
        CompanyMember member = getCurrentMember(authentication);
        JobPosting job = jobPostingService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다."));
        
        if (!job.getCompanyMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
        
        // 필드 업데이트
        job.setTitle(updatedJob.getTitle());
        job.setStartDate(updatedJob.getStartDate());
        job.setEndDate(updatedJob.getEndDate());
        job.setJobForm(updatedJob.getJobForm());
        job.setJobType(updatedJob.getJobType());
        job.setJobCategory(updatedJob.getJobCategory());
        job.setIndustry(updatedJob.getIndustry());
        job.setWorkTime(updatedJob.getWorkTime());
        job.setRoleLevel(updatedJob.getRoleLevel());
        job.setBaseSalary(updatedJob.getBaseSalary());
        job.setExperience(updatedJob.getExperience());
        job.setJobLocation(updatedJob.getJobLocation());
        job.setCompanyIntro(updatedJob.getCompanyIntro());
        job.setPositionSummary(updatedJob.getPositionSummary());
        job.setSkillQualification(updatedJob.getSkillQualification());
        job.setBenefits(updatedJob.getBenefits());
        job.setNotes(updatedJob.getNotes());
        job.setCompanyType(updatedJob.getCompanyType());
        job.setEstablishedDate(updatedJob.getEstablishedDate());
        job.setEmployeeNum(updatedJob.getEmployeeNum());
        job.setCapital(updatedJob.getCapital());
        job.setRevenue(updatedJob.getRevenue());
        job.setHomepage(updatedJob.getHomepage());
        
        if ("temp".equals(action)) {
            job.setPostingYn("2");
            redirectAttributes.addFlashAttribute("successMessage", "임시저장되었습니다.");
        } else {
            job.setPostingYn("1");
            redirectAttributes.addFlashAttribute("successMessage", "채용공고가 수정되었습니다.");
        }
        
        jobPostingService.update(job);
        return "redirect:/company/jobs";
    }

    // 채용공고 삭제
    @PostMapping("/jobs/{id}/delete")
    public String deleteJob(@PathVariable Long id,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        CompanyMember member = getCurrentMember(authentication);
        JobPosting job = jobPostingService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다."));
        
        if (!job.getCompanyMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        
        jobPostingService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "채용공고가 삭제되었습니다.");
        return "redirect:/company/jobs";
    }

    // 지원자 관리
    @GetMapping("/jobs/{id}/applicants")
    public String applicants(@PathVariable Long id,
                             @RequestParam(required = false) Integer status,
                             Authentication authentication, Model model) {
        CompanyMember member = getCurrentMember(authentication);
        JobPosting job = jobPostingService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다."));
        
        if (!job.getCompanyMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("조회 권한이 없습니다.");
        }
        
        List<Application> applications;
        if (status != null) {
            applications = applicationService.findByJobPostingIdAndReviewStatus(id, status);
        } else {
            applications = applicationService.findByJobPostingId(id);
        }
        
        model.addAttribute("job", job);
        model.addAttribute("applications", applications);
        model.addAttribute("selectedStatus", status);
        
        return "company/applicants";
    }

    // 심사상태 변경
    @PostMapping("/applicants/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam Integer status,
                               @RequestParam Long jobId,
                               RedirectAttributes redirectAttributes) {
        applicationService.updateReviewStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "심사상태가 변경되었습니다.");
        return "redirect:/company/jobs/" + jobId + "/applicants";
    }
}
