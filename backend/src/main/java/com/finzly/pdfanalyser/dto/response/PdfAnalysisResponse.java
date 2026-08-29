package com.finzly.pdfanalyser.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Structured analysis result returned to the client after LLM processing.
 * All fields correspond exactly to what is displayed on the frontend.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PdfAnalysisResponse(

        String documentType,
        String title,
        String authors,
        String summary,
        String keyTakeaway

) {}
