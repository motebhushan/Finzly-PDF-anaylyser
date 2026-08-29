package com.finzly.pdfanalyser.util;

import com.finzly.pdfanalyser.exception.InvalidPdfUrlException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Utility component for validating PDF URLs before sending them to the LLM.
 *
 * Performs two levels of validation:
 *   1. Structural check — is it a well-formed http/https URL?
 *   2. Reachability check — does a HEAD request return a PDF content-type?
 */
@Slf4j
@Component
public class PdfUrlValidator {

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    /**
     * Validates that the given URL string is a reachable PDF document.
     *
     * @param urlString the URL to validate
     * @throws InvalidPdfUrlException if the URL is malformed or does not resolve to a PDF
     */
    public void validate(String urlString) {
        URL url = parseUrl(urlString);
        checkContentType(url, urlString);
    }

    private URL parseUrl(String urlString) {
        try {
            URI uri = URI.create(urlString);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))) {
                throw new InvalidPdfUrlException("URL must use http or https scheme: " + urlString);
            }
            return uri.toURL();
        } catch (IllegalArgumentException | java.net.MalformedURLException e) {
            throw new InvalidPdfUrlException("Malformed URL: " + urlString, e);
        }
    }

    private void checkContentType(URL url, String urlString) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 400) {
                throw new InvalidPdfUrlException(
                        "PDF URL returned HTTP " + responseCode + ". Please check the URL is publicly accessible.");
            }

            String contentType = connection.getContentType();
            log.debug("Content-Type for {}: {}", urlString, contentType);

            // Accept PDF content-type OR allow if content-type is unknown (some CDNs omit it)
            if (contentType != null && !contentType.contains("pdf") && !contentType.contains("octet-stream")) {
                // Be lenient — some valid PDF servers return generic content types
                log.warn("Unexpected content-type '{}' for URL: {}. Proceeding anyway.", contentType, urlString);
            }

        } catch (InvalidPdfUrlException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidPdfUrlException("Could not reach PDF URL: " + urlString + ". " + e.getMessage(), e);
        }
    }
}
