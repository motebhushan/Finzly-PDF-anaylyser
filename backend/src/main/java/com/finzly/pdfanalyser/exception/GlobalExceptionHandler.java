package com.finzly.pdfanalyser.exception;

import com.finzly.pdfanalyser.dto.response.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler — single place to intercept all exceptions thrown
 * anywhere in the application and convert them into consistent ApiErrorResponse payloads.
 *
 * Why @RestControllerAdvice?
 *   - Centralises error handling (no try/catch in controllers)
 *   - Ensures every error response has the same JSON shape
 *   - Logs server-side context without leaking it to the client
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles Bean Validation failures (@Valid on request body).
     * Collects all field errors into a readable message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("Validation failed: {}", message);
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(400, "Validation Error", message));
    }

    /**
     * Handles invalid or unreachable PDF URL — user error (400).
     */
    @ExceptionHandler(InvalidPdfUrlException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPdfUrl(InvalidPdfUrlException ex) {
        log.warn("Invalid PDF URL: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(400, "Bad Request", ex.getMessage()));
    }

    /**
     * Handles Gemini API failures — upstream dependency issue (502).
     */
    @ExceptionHandler(LlmApiException.class)
    public ResponseEntity<ApiErrorResponse> handleLlmApiException(LlmApiException ex) {
        log.error("LLM API error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiErrorResponse.of(502, "Bad Gateway", "LLM analysis service is currently unavailable. Please try again."));
    }

    /**
     * Fallback handler for any unexpected exception — never expose internal details (500).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError()
                .body(ApiErrorResponse.of(500, "Internal Server Error", "An unexpected error occurred. Please try again later."));
    }
}
