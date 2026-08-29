package com.finzly.pdfanalyser.service;

import com.finzly.pdfanalyser.dto.request.PdfAnalyseRequest;
import com.finzly.pdfanalyser.dto.response.PdfAnalysisResponse;

/**
 * Business logic contract for PDF analysis.
 * Decouples the controller from the concrete implementation.
 */
public interface PdfAnalyserService {

    /** Validates the PDF URL and delegates analysis to the LLM client. */
    PdfAnalysisResponse analyse(PdfAnalyseRequest request);

    /** Analyses a PDF from raw bytes (local file upload — no URL needed). */
    PdfAnalysisResponse analyseUpload(byte[] pdfBytes, String originalFilename);
}
