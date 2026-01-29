package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("message", "MSA 프로젝트에 오신 것을 환영합니다");
        return "index";
    }

    // personal-service
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("message", "로그인");
        return "personal/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("message", "회원 가입");
        return "personal/register";
    }

    @GetMapping("/search-id")
    public String searchId(Model model) {
        model.addAttribute("message", "아이디 찾기");
        return "personal/search-id";
    }

    @GetMapping("/search-pw")
    public String searchPw(Model model) {
        model.addAttribute("message", "비밀번호 찾기");
        return "personal/search-pw";
    }

    @GetMapping("/reset-pw")
    public String resetPw(Model model) {
        model.addAttribute("message", "비밀번호 재설정");
        return "personal/reset-pw";
    }

    @GetMapping("/mypage")
    public String mypage(Model model) {
        model.addAttribute("message", "마이페이지");
        return "personal/mypage";
    }

    @GetMapping("/intro")
    public String intro(Model model) {
        model.addAttribute("message", "소개페이지");
        return "intro";
    }

    // admin-service
    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("message", "관리자 페이지");
        return "admin/admin";
    }

    @GetMapping("/admin/user_list")
    public String adminUserList() {
        return "admin/admin_user_list"; // templates/admin_user_list.html
    }

    @GetMapping("/admin/user_detail")
    public String adminUserDetail() {
        return "admin/admin_user_detail"; // templates/admin_user_detail.html
    }

    @GetMapping("/admin/admin_list")
    public String adminAdminList() {
        return "admin/admin_admin_list"; // templates/admin_admin_list.html
    }

    @GetMapping("/admin/admin_write")
    public String adminWritePage() {
        return "admin/admin_admin_write"; // templates/admin_admin_write.html
    }

    @GetMapping("/admin/company_list")
    public String adminCompanyList() {
        return "admin/admin_company_list";
    }

    @GetMapping("/admin/company_detail")
    public String adminCompanyDetail() {
        return "admin/admin_company_detail"; // templates/admin_company_detail.html
    }

    @GetMapping("/admin/stats")
    public String adminStats() {
        return "admin/admin_stats";
    }

    @GetMapping("/admin/admin_modify")
    public String adminModifyPage() {
        return "admin/admin_admin_modify"; // templates/admin_admin_modify.html
    }


    // company-service
    @GetMapping("/company/login")
    public String companyLogin() {
        return "company/company_login";
    }

    @GetMapping("/company/register")
    public String companyRegister() {
        return "company/company_register";
    }

    @GetMapping("/company/mypage")
    public String companyMypage() {
        return "company/company_mypage";
    }

    @GetMapping("/company/search-id")
    public String companySearchId() {
        return "company/company_search_id";
    }

    @GetMapping("/company/search-pw")
    public String companySearchPw() {
        return "company/company_search_pw";
    }

    @GetMapping("/company/jobs")
    public String jobs(Model model) {
        return "company/company_jobs";
    }

    @GetMapping("/company/jobs/write")
    public String jobWrite() {
        return "company/company_jobwrite";
    }

    @GetMapping("/company/jobs/detail")
    public String jobDetail(@RequestParam("id") Long id, Model model) {
        model.addAttribute("jobId", id);
        return "company/company_jobsdetail";
    }

    @GetMapping("/jobs")
    public String jobsPublic() {
        return "company/jobs";
    }

    @GetMapping("/jobs/detail")
    public String publicJobDetailPage(@RequestParam("id") Long id, Model model) {
        model.addAttribute("jobId", id);
        return "company/jobsdetail";
    }

    @GetMapping("/apply")
    public String apply() {
        return "personal/apply";
    }

    @GetMapping("/resume-popup")
    public String resumePopup() {
        return "personal/resume-popup";
    }

    @GetMapping("/applystatus")
    public String applyStatus() {
        return "personal/applystatus";
    }

}