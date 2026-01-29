package com.example.admin.config;

import com.example.admin.service.AdminUserService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap {

    private final AdminUserService adminUserService;

    public AdminBootstrap(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostConstruct
    public void init() {
        adminUserService.ensureDefaultSuperAdmin();
    }
}
