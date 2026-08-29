package com.finzly.pdfanalyser.service;

import com.finzly.pdfanalyser.client.GeminiClient;
import com.finzly.pdfanalyser.dto.request.PdfAnalyseRequest;
import com.finzly.pdfanalyser.dto.response.PdfAnalysisResponse;
import com.finzly.pdfanalyser.util.PdfUrlValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Orchestrates PDF analysis:
 *   1. Validate the PDF URL (structural + reachability)
 *   2. Delegate to the LLM client for analysis
 *
 * The service layer intentionally contains no HTTP or JSON logic — that lives
 * in the client layer. This keeps each layer focused and independently testable.
 */
@Slf4j
@Service
public class PdfAnalyserServiceImpl implements PdfAnalyserService {

    private final PdfUrlValidator pdfUrlValidator;
    private final GeminiClient geminiClient;

    public PdfAnalyserServiceImpl(PdfUrlValidator pdfUrlValidator, GeminiClient geminiClient) {
        this.pdfUrlValidator = pdfUrlValidator;
        this.geminiClient = geminiClient;
    }

    @Override
    public PdfAnalysisResponse analyse(PdfAnalyseRequest request) {
        String pdfUrl = request.pdfUrl();
        log.info("Starting PDF analysis for URL: {}", pdfUrl);
        pdfUrlValidator.validate(pdfUrl);
        PdfAnalysisResponse response = geminiClient.analyse(pdfUrl);
        log.info("Analysis complete for URL: {}", pdfUrl);
        return response;
    }

    @Override
    public PdfAnalysisResponse analyseUpload(byte[] pdfBytes, String originalFilename) {
        log.info("Starting PDF analysis for uploaded file: {} ({} bytes)", originalFilename, pdfBytes.length);

        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new com.finzly.pdfanalyser.exception.InvalidPdfUrlException("Uploaded file is empty.");
        }
        if (pdfBytes.length > 20 * 1024 * 1024) { // 20 MB limit
            throw new com.finzly.pdfanalyser.exception.InvalidPdfUrlException(
                    "File too large. Maximum allowed size is 20 MB.");
        }

        PdfAnalysisResponse response = geminiClient.analyseBytes(pdfBytes);
        log.info("Analysis complete for uploaded file: {}", originalFilename);
        return response;
    }

}
