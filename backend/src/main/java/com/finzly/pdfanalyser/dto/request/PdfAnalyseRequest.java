package com.finzly.pdfanalyser.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for PDF analysis endpoint.
 * Carries the publicly accessible PDF URL submitted by the client.
 */
public record PdfAnalyseRequest(

        @NotBlank(message = "PDF URL must not be blank")
        @Pattern(
                regexp = "^(https?://).+",
                message = "PDF URL must start with http:// or https://"
        )
        String pdfUrl

) {}
