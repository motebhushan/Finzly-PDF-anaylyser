package com.finzly.pdfanalyser.controller;

import com.finzly.pdfanalyser.dto.request.PdfAnalyseRequest;
import com.finzly.pdfanalyser.dto.response.PdfAnalysisResponse;
import com.finzly.pdfanalyser.service.PdfAnalyserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST controller exposing PDF analysis endpoints.
 * Keeps thin — all business logic lives in PdfAnalyserService.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/pdf")
public class PdfAnalyserController {

    private final PdfAnalyserService pdfAnalyserService;

    public PdfAnalyserController(PdfAnalyserService pdfAnalyserService) {
        this.pdfAnalyserService = pdfAnalyserService;
    }

    /** POST /api/v1/pdf/analyse — analyse a PDF from a public URL */
    @PostMapping("/analyse")
    public ResponseEntity<PdfAnalysisResponse> analyse(@Valid @RequestBody PdfAnalyseRequest request) {
        log.info("Received analysis request for URL: {}", request.pdfUrl());
        return ResponseEntity.ok(pdfAnalyserService.analyse(request));
    }

    /** POST /api/v1/pdf/upload — analyse a locally uploaded PDF file */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<PdfAnalysisResponse> upload(
            @RequestParam("file") MultipartFile file) throws IOException {

        log.info("Received file upload: {} ({} bytes)", file.getOriginalFilename(), file.getSize());

        String contentType = file.getContentType();
        if (contentType == null || !contentType.contains("pdf")) {
            return ResponseEntity.badRequest().build();
        }

        PdfAnalysisResponse response = pdfAnalyserService.analyseUpload(
                file.getBytes(), file.getOriginalFilename());
        return ResponseEntity.ok(response);
    }

    /** GET /api/v1/pdf/health — deployment health check */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("PDF Analyser is running");
    }
}

