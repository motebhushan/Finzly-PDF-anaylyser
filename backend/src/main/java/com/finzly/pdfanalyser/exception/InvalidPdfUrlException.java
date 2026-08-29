package com.finzly.pdfanalyser.exception;

/**
 * Thrown when the user-supplied PDF URL is malformed, unreachable,
 * or does not resolve to a PDF document.
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidPdfUrlException extends RuntimeException {

    public InvalidPdfUrlException(String message) {
        super(message);
    }

    public InvalidPdfUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
