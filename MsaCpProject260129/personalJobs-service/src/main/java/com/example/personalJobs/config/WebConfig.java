package com.example.personalJobs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        Path p = Paths.get(uploadPath).toAbsolutePath().normalize();
        String location = p.toUri().toString();
        if (!location.endsWith("/")) location += "/"; // ✅ 중요

        System.out.println("========================================");
        System.out.println("정적 리소스 경로 설정:");
        System.out.println("Upload Path: " + uploadPath);
        System.out.println("Absolute Path: " + location);
        System.out.println("========================================");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(0); // ✅ 개발/테스트 중 캐시 꺼두기
    }
}
