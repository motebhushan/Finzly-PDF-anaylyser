package com.finzly.pdfanalyser.dto.response;

import java.time.Instant;

/**
 * Standardised error envelope returned for all 4xx / 5xx responses.
 * Consistent shape makes it easy for the frontend to handle all errors uniformly.
 */
public record ApiErrorResponse(

        int status,
        String error,
        String message,
        Instant timestamp

) {
    /**
     * Convenience factory — timestamp defaults to now.
     */
    public static ApiErrorResponse of(int status, String error, String message) {
        return new ApiErrorResponse(status, error, message, Instant.now());
    }
}
