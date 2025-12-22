package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/personal/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final PersonalMemberService personalMemberService;
    private final ResumeService resumeService;

    // 현재 로그인한 회원 조회
    private PersonalMember getCurrentMember(Authentication authentication) {
        String loginId = authentication.getName();
        return personalMemberService.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

    // 이력서 목록
    @GetMapping
    public String list(Authentication authentication, Model model) {
        PersonalMember member = getCurrentMember(authentication);
        List<Resume> resumes = resumeService.findByPersonalMemberId(member.getId());
        
        model.addAttribute("resumes", resumes);
        return "personal/resumes/list";
    }

    // 이력서 등록 폼
    @GetMapping("/new")
    public String newForm(Authentication authentication, Model model) {
        PersonalMember member = getCurrentMember(authentication);
        
        Resume resume = new Resume();
        // 회원 정보로 기본값 설정
        resume.setEmail(member.getEmail());
        
        model.addAttribute("resume", resume);
        model.addAttribute("member", member);
        return "personal/resumes/form";
    }

    // 이력서 등록 처리
    @PostMapping("/new")
    public String create(Authentication authentication,
                         @ModelAttribute Resume resume,
                         @RequestParam(required = false) String action,
                         RedirectAttributes redirectAttributes) {
        PersonalMember member = getCurrentMember(authentication);
        resume.setPersonalMember(member);
        resume.setInsertDate(LocalDate.now());
        
        if ("temp".equals(action)) {
            resume.setApplyYn("2"); // 임시저장
            redirectAttributes.addFlashAttribute("successMessage", "이력서가 임시저장되었습니다.");
        } else {
            resume.setApplyYn("1"); // 지원용
            redirectAttributes.addFlashAttribute("successMessage", "이력서가 등록되었습니다.");
        }
        
        resumeService.save(resume);
        return "redirect:/personal/resumes";
    }

    // 이력서 상세보기
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication authentication, Model model) {
        PersonalMember member = getCurrentMember(authentication);
        Resume resume = resumeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));
        
        // 본인 이력서인지 확인
        if (!resume.getPersonalMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("조회 권한이 없습니다.");
        }
        
        model.addAttribute("resume", resume);
        return "personal/resumes/detail";
    }

    // 이력서 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication authentication, Model model) {
        PersonalMember member = getCurrentMember(authentication);
        Resume resume = resumeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));
        
        if (!resume.getPersonalMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
        
        model.addAttribute("resume", resume);
        model.addAttribute("member", member);
        return "personal/resumes/form";
    }

    // 이력서 수정 처리
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         Authentication authentication,
                         @ModelAttribute Resume updatedResume,
                         @RequestParam(required = false) String action,
                         RedirectAttributes redirectAttributes) {
        PersonalMember member = getCurrentMember(authentication);
        Resume resume = resumeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));
        
        if (!resume.getPersonalMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
        
        // 필드 업데이트
        resume.setEmail(updatedResume.getEmail());
        resume.setPhone(updatedResume.getPhone());
        resume.setAddress(updatedResume.getAddress());
        resume.setSchool(updatedResume.getSchool());
        resume.setMajor(updatedResume.getMajor());
        resume.setEnrollDate(updatedResume.getEnrollDate());
        resume.setGraduateDate(updatedResume.getGraduateDate());
        resume.setGpa(updatedResume.getGpa());
        resume.setSkill(updatedResume.getSkill());
        resume.setSelfIntro(updatedResume.getSelfIntro());
        resume.setUpdateDate(LocalDate.now());
        
        if ("temp".equals(action)) {
            resume.setApplyYn("2");
            redirectAttributes.addFlashAttribute("successMessage", "이력서가 임시저장되었습니다.");
        } else {
            resume.setApplyYn("1");
            redirectAttributes.addFlashAttribute("successMessage", "이력서가 수정되었습니다.");
        }
        
        resumeService.save(resume);
        return "redirect:/personal/resumes";
    }

    // 이력서 삭제
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        PersonalMember member = getCurrentMember(authentication);
        Resume resume = resumeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));
        
        if (!resume.getPersonalMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        
        resumeService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "이력서가 삭제되었습니다.");
        return "redirect:/personal/resumes";
    }
}
