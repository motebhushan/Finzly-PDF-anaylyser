package com.finzly.pdfanalyser.exception;

/**
 * Thrown when the Gemini LLM API call fails — network error,
 * rate limit exceeded, invalid API key, or unexpected response format.
 * Maps to HTTP 502 Bad Gateway (upstream dependency failure).
 */
public class LlmApiException extends RuntimeException {

    public LlmApiException(String message) {
        super(message);
    }

    public LlmApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
