package com.finzly.pdfanalyser.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Application-level bean configuration.
 *
 * RestClient is the modern Spring 6.1+ replacement for RestTemplate.
 * A shared, injectable RestClient bean is provided here so callers
 * don't instantiate HTTP clients ad hoc.
 */
@Configuration
public class AppConfig {

    /**
     * Shared RestClient bean used by GeminiClientImpl.
     * RestClient is thread-safe and designed to be shared.
     */
    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    /**
     * Shared ObjectMapper bean used for JSON serialisation/deserialisation.
     * FAIL_ON_UNKNOWN_PROPERTIES disabled so extra fields from Gemini API don't cause errors.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}

