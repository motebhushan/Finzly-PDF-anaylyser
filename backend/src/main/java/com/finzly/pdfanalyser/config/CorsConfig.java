package com.finzly.pdfanalyser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration — restricts which origins can call the backend API.
 *
 * Why is this needed?
 *   Browsers block cross-origin requests by default (Same-Origin Policy).
 *   The React frontend (Vercel domain) is a different origin from the Spring Boot
 *   backend (Render domain), so we must explicitly allow it here.
 *
 *   In production the allowed origin is set via the CORS_ALLOWED_ORIGINS env var.
 */
@Configuration
public class CorsConfig {

    private final CorsProperties corsProperties;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] origins = corsProperties.allowedOrigins().toArray(String[]::new);
                registry.addMapping("/api/**")
                        .allowedOrigins(origins)
                        .allowedMethods("GET", "POST", "OPTIONS")
                        .allowedHeaders("*")
                        .maxAge(3600);
            }
        };
    }
}
