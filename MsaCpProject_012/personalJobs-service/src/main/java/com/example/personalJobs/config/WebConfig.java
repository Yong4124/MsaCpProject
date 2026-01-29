package com.example.personalJobs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // uploads 폴더의 절대 경로
        String absolutePath = Paths.get(uploadPath).toAbsolutePath().toUri().toString();

        System.out.println("========================================");
        System.out.println("정적 리소스 경로 설정:");
        System.out.println("Upload Path: " + uploadPath);
        System.out.println("Absolute Path: " + absolutePath);
        System.out.println("========================================");

        // /uploads/** 요청을 실제 파일 시스템으로 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absolutePath);
    }
}