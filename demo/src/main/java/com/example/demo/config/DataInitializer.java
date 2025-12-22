package com.example.demo.config;

import com.example.demo.entity.Admin;
import com.example.demo.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 관리자 계정이 없으면 생성
        if (adminRepository.count() == 0) {
            Admin admin = Admin.builder()
                    .loginId("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .name("시스템관리자")
                    .email("admin@usfkjobs.com")
                    .phone("02-1234-5678")
                    .department("시스템관리팀")
                    .auth("1")  // 1: 슈퍼관리자
                    .insertDate(LocalDate.now())
                    .delYn("N")
                    .build();

            adminRepository.save(admin);
            log.info("===========================================");
            log.info("관리자 계정이 생성되었습니다.");
            log.info("아이디: admin");
            log.info("비밀번호: admin123");
            log.info("===========================================");
        }
    }
}