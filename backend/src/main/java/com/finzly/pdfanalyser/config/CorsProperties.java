package com.finzly.pdfanalyser.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

/**
 * Type-safe binding for the 'app.cors.*' properties.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        List<String> allowedOrigins
) {}
