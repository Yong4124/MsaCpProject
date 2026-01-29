package com.example.personalJobs.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MVCController {

    @GetMapping("/jobs")
    public String jobs(HttpSession session, Model model) {

        boolean isLoggedIn = session.getAttribute("MEMB_PK") != null;
        model.addAttribute("isLoggedIn", isLoggedIn);

        return "personalJobs"; // templates/personalJobs.html
    }
}
