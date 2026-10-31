package com.example.ticktracker.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Path uploadPath;

    public WebConfig(@Value("${app.upload-dir}") String uploadDir) {
        this.uploadPath = Path.of(uploadDir).toAbsolutePath();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create upload directory", e);
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOriginPatterns("*").allowedMethods("GET", "POST");
        registry.addMapping("/uploads/**").allowedOriginPatterns("*").allowedMethods("GET");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**").addResourceLocations(uploadPath.toUri().toString());
    }
}