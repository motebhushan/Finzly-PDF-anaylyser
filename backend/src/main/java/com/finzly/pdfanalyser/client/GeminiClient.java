package com.finzly.pdfanalyser.client;

import com.finzly.pdfanalyser.dto.response.PdfAnalysisResponse;

/**
 * Contract for calling the LLM API.
 *
 * Programming to an interface (not the concrete implementation) allows:
 *   - Easy mocking in unit tests
 *   - Swapping LLM providers without touching the service layer
 */
public interface GeminiClient {

    /**
     * Analyses a PDF from a publicly accessible URL.
     * Downloads the PDF bytes internally before processing.
     */
    PdfAnalysisResponse analyse(String pdfUrl);

    /**
     * Analyses a PDF directly from raw bytes (e.g. from a local file upload).
     * Skips the download step and goes straight to the Gemini Files API upload.
     */
    PdfAnalysisResponse analyseBytes(byte[] pdfBytes);
}
