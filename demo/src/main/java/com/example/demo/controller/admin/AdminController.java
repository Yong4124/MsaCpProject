package com.example.demo.controller.admin;

import com.example.demo.entity.*;
import com.example.demo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final PersonalMemberService personalMemberService;
    private final CompanyMemberService companyMemberService;
    private final JobPostingService jobPostingService;
    private final ApplicationService applicationService;

    // 대시보드
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<PersonalMember> personalMembers = personalMemberService.findAll();
        List<CompanyMember> companyMembers = companyMemberService.findAll();
        List<JobPosting> jobPostings = jobPostingService.findAll();
        
        // 승인 대기 수
        long pendingPersonal = personalMembers.stream()
                .filter(m -> "N".equals(m.getApprovalYn()))
                .count();
        long pendingCompany = companyMembers.stream()
                .filter(m -> "N".equals(m.getApprovalYn()))
                .count();
        
        model.addAttribute("menu", "dashboard");
        model.addAttribute("totalPersonal", personalMembers.size());
        model.addAttribute("totalCompany", companyMembers.size());
        model.addAttribute("totalJobs", jobPostings.size());
        model.addAttribute("pendingPersonal", pendingPersonal);
        model.addAttribute("pendingCompany", pendingCompany);
        model.addAttribute("recentPersonal", personalMembers.stream().limit(5).toList());
        model.addAttribute("recentCompany", companyMembers.stream().limit(5).toList());
        
        return "admin/dashboard";
    }

    // 개인회원 관리
    @GetMapping("/members/personal")
    public String personalMembers(@RequestParam(required = false) String status, Model model) {
        List<PersonalMember> members;
        if ("pending".equals(status)) {
            members = personalMemberService.findPendingApproval();
        } else {
            members = personalMemberService.findAll();
        }
        
        model.addAttribute("menu", "personal");
        model.addAttribute("members", members);
        model.addAttribute("selectedStatus", status);
        return "admin/members-personal";
    }

    // 개인회원 승인
    @PostMapping("/members/personal/{id}/approve")
    public String approvePersonal(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        personalMemberService.approve(id);
        redirectAttributes.addFlashAttribute("successMessage", "회원이 승인되었습니다.");
        return "redirect:/admin/members/personal";
    }

    // 개인회원 삭제
    @PostMapping("/members/personal/{id}/delete")
    public String deletePersonal(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        personalMemberService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "회원이 삭제되었습니다.");
        return "redirect:/admin/members/personal";
    }

    // 기업회원 관리
    @GetMapping("/members/company")
    public String companyMembers(@RequestParam(required = false) String status, Model model) {
        List<CompanyMember> members;
        if ("pending".equals(status)) {
            members = companyMemberService.findPendingApproval();
        } else {
            members = companyMemberService.findAll();
        }
        
        model.addAttribute("menu", "company");
        model.addAttribute("members", members);
        model.addAttribute("selectedStatus", status);
        return "admin/members-company";
    }

    // 기업회원 승인
    @PostMapping("/members/company/{id}/approve")
    public String approveCompany(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        companyMemberService.approve(id);
        redirectAttributes.addFlashAttribute("successMessage", "회원이 승인되었습니다.");
        return "redirect:/admin/members/company";
    }

    // 기업회원 삭제
    @PostMapping("/members/company/{id}/delete")
    public String deleteCompany(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        companyMemberService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "회원이 삭제되었습니다.");
        return "redirect:/admin/members/company";
    }

    // 채용공고 관리
    @GetMapping("/jobs")
    public String jobs(Model model) {
        List<JobPosting> jobPostings = jobPostingService.findAll();
        
        model.addAttribute("menu", "jobs");
        model.addAttribute("jobPostings", jobPostings);
        return "admin/jobs";
    }

    // 채용공고 삭제
    @PostMapping("/jobs/{id}/delete")
    public String deleteJob(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        jobPostingService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "채용공고가 삭제되었습니다.");
        return "redirect:/admin/jobs";
    }

    // 지원 관리
    @GetMapping("/applications")
    public String applications(Model model) {
        List<JobPosting> jobPostings = jobPostingService.findAll();
        
        model.addAttribute("menu", "applications");
        model.addAttribute("jobPostings", jobPostings);
        return "admin/applications";
    }
}
