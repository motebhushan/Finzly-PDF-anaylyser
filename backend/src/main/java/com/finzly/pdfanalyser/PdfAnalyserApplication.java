package com.finzly.pdfanalyser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * PDF Analyser Application Entry Point.
 *
 * @ConfigurationPropertiesScan automatically detects and registers all
 * @ConfigurationProperties classes (GeminiProperties, CorsProperties)
 * without needing @EnableConfigurationProperties on each one.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PdfAnalyserApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdfAnalyserApplication.class, args);
    }

}
