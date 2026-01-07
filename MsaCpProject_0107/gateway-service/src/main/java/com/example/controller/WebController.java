package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("message", "회원 가입");
        return "register";
    }

    @GetMapping("/search-id")
    public String searchId(Model model) {
        model.addAttribute("message", "아이디 찾기");
        return "search-id";
    }

    @GetMapping("/search-pw")
    public String searchPw(Model model) {
        model.addAttribute("message", "비밀번호 찾기");
        return "search-pw";
    }

    @GetMapping("/reset-pw")
    public String resetPw(Model model) {
        model.addAttribute("message", "비밀번호 재설정");
        return "reset-pw";
    }

    @GetMapping("/mypage")
    public String mypage(Model model) {
        model.addAttribute("message", "마이페이지");
        return "mypage";
    }

    // admin-service
    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("message", "관리자 페이지");
        return "admin";
    }

    @GetMapping("/admin/user_list")
    public String adminUserList() {
        return "admin_user_list"; // templates/admin_user_list.html
    }

    @GetMapping("/admin/admin_list")
    public String adminAdminList() {
        return "admin_admin_list"; // templates/admin_admin_list.html
    }

    @GetMapping("/admin/admin_write")
    public String adminWritePage() {
        return "admin_admin_write"; // templates/admin_admin_write.html
    }

    @GetMapping("/admin/company_list")
    public String adminCompanyList() {
        return "admin_company_list";
    }

    @GetMapping("/admin/company_detail")
    public String adminCompanyDetail() {
        return "admin_company_detail"; // templates/admin_company_detail.html
    }

    @GetMapping("/admin/stats")
    public String adminStats() {
        return "admin_stats";
    }

    @GetMapping("/admin/admin_modify")
    public String adminModifyPage() {
        return "admin_admin_modify"; // templates/admin_admin_modify.html
    }

    @GetMapping("/company/login")
    public String companyLogin() {
        return "company_login";
    }

    @GetMapping("/company/register")
    public String companyRegister() {
        return "company_register";
    }

    @GetMapping("/company/mypage")
    public String companyMypage() {
        return "company_mypage";
    }

    @GetMapping("/company/search-id")
    public String companySearchId() {
        return "company_search_id";
    }

    @GetMapping("/company/search-pw")
    public String companySearchPw() {
        return "company_search_pw";
    }

    @GetMapping("/company/jobs")
    public String companyJobs() {
        return "company_jobs";
    }


}