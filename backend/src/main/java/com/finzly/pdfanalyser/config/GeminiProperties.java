package com.finzly.pdfanalyser.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe binding for the 'app.gemini.*' properties defined in application.yml.
 *
 * Using @ConfigurationProperties is the industry-standard approach — it gives
 * compile-time safety, IDE auto-complete, and easy testability compared to @Value.
 */
@ConfigurationProperties(prefix = "app.gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        String apiUrl,
        int timeoutSeconds
) {}
