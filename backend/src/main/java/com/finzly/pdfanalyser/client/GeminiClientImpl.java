package com.finzly.pdfanalyser.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finzly.pdfanalyser.config.GeminiProperties;
import com.finzly.pdfanalyser.dto.response.PdfAnalysisResponse;
import com.finzly.pdfanalyser.exception.InvalidPdfUrlException;
import com.finzly.pdfanalyser.exception.LlmApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Concrete implementation of GeminiClient that calls Google's Gemini API.
 *
 * PDF Handling Strategy — Gemini Files API (2-step):
 *   Step 1: Download PDF bytes and upload to Gemini Files API → get a managed file_uri
 *   Step 2: Call generateContent referencing that file_uri via file_data
 *
 * This is the correct approach for newer Gemini models (gemini-3.6-flash etc.)
 * which require proper file references rather than raw inline base64.
 */
@Slf4j
@Component
public class GeminiClientImpl implements GeminiClient {

    private static final String ANALYSIS_PROMPT = """
            You are a document analysis assistant. Analyse the PDF document provided and return ONLY a valid JSON object with exactly these five fields:
            {
              "documentType": "<type of document e.g. Research Paper, Report, Manual>",
              "title": "<exact title of the document>",
              "authors": "<author(s) name(s), comma-separated>",
              "summary": "<2-3 sentence summary of the document>",
              "keyTakeaway": "<the single most important insight or conclusion>"
            }
            Do NOT include any markdown, explanation, or extra text. Return only the raw JSON object.
            """;

    private static final String FILES_UPLOAD_URL =
            "https://generativelanguage.googleapis.com/upload/v1beta/files";

    private final RestClient restClient;
    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiClientImpl(RestClient restClient,
                             GeminiProperties geminiProperties,
                             ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.geminiProperties = geminiProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public PdfAnalysisResponse analyse(String pdfUrl) {
        log.info("Downloading PDF from: {}", pdfUrl);
        byte[] pdfBytes = downloadPdf(pdfUrl);
        log.info("PDF downloaded ({} bytes).", pdfBytes.length);

        String fileUri = uploadToGeminiFiles(pdfBytes);
        log.info("PDF uploaded to Gemini Files API. fileUri: {}", fileUri);

        String responseBody = callGenerateContent(fileUri);
        return parseResponse(responseBody);
    }

    @Override
    public PdfAnalysisResponse analyseBytes(byte[] pdfBytes) {
        log.info("Analysing locally uploaded PDF ({} bytes).", pdfBytes.length);

        String fileUri = uploadToGeminiFiles(pdfBytes);
        log.info("PDF uploaded to Gemini Files API. fileUri: {}", fileUri);

        String responseBody = callGenerateContent(fileUri);
        return parseResponse(responseBody);
    }

    // ─── Step 1: Download PDF ─────────────────────────────────────────────────

    private byte[] downloadPdf(String pdfUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(pdfUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() < 200 || response.statusCode() >= 400) {
                throw new InvalidPdfUrlException(
                        "Could not download PDF — server returned HTTP " + response.statusCode());
            }

            byte[] bytes = response.body();
            if (bytes == null || bytes.length == 0) {
                throw new InvalidPdfUrlException("Downloaded file is empty from URL: " + pdfUrl);
            }
            return bytes;

        } catch (InvalidPdfUrlException e) {
            throw e;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InvalidPdfUrlException(
                    "Failed to download PDF from: " + pdfUrl + ". " + e.getMessage(), e);
        }
    }

    // ─── Step 2: Upload to Gemini Files API ──────────────────────────────────

    /**
     * Uploads PDF bytes to the Gemini Files API using multipart upload.
     * Returns the managed file URI (e.g. "https://generativelanguage.googleapis.com/v1beta/files/xxx")
     * which can then be referenced in generateContent calls.
     */
    private String uploadToGeminiFiles(byte[] pdfBytes) {
        String boundary = "boundary-" + UUID.randomUUID().toString().replace("-", "");
        String apiKey = geminiProperties.apiKey();

        // Build multipart body: metadata part + binary PDF part
        String metadataPart =
                "--" + boundary + "\r\n" +
                "Content-Type: application/json; charset=utf-8\r\n\r\n" +
                "{\"file\":{\"display_name\":\"pdf-analysis\"}}\r\n" +
                "--" + boundary + "\r\n" +
                "Content-Type: application/pdf\r\n\r\n";

        String closingBoundary = "\r\n--" + boundary + "--";

        byte[] metaBytes = metadataPart.getBytes();
        byte[] closeBytes = closingBoundary.getBytes();

        byte[] body = new byte[metaBytes.length + pdfBytes.length + closeBytes.length];
        System.arraycopy(metaBytes, 0, body, 0, metaBytes.length);
        System.arraycopy(pdfBytes, 0, body, metaBytes.length, pdfBytes.length);
        System.arraycopy(closeBytes, 0, body, metaBytes.length + pdfBytes.length, closeBytes.length);

        try {
            String uploadUrl = FILES_UPLOAD_URL + "?key=" + apiKey;

            String responseBody = restClient.post()
                    .uri(uploadUrl)
                    .header("Content-Type", "multipart/related; boundary=" + boundary)
                    .header("X-Goog-Upload-Protocol", "multipart")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            String fileUri = root.path("file").path("uri").asText(null);

            if (fileUri == null || fileUri.isBlank()) {
                log.error("Files API response missing file.uri: {}", responseBody);
                throw new LlmApiException("Gemini Files API did not return a file URI.");
            }
            return fileUri;

        } catch (LlmApiException e) {
            throw e;
        } catch (RestClientException | IOException e) {
            log.error("Failed to upload PDF to Gemini Files API: {}", e.getMessage(), e);
            throw new LlmApiException("Failed to upload PDF to Gemini Files API: " + e.getMessage(), e);
        }
    }

    // ─── Step 3: Call generateContent ─────────────────────────────────────────

    /**
     * Calls the Gemini generateContent endpoint referencing the uploaded file via file_data.
     */
    private String callGenerateContent(String fileUri) {
        Map<String, Object> filePart = Map.of(
                "file_data", Map.of(
                        "mime_type", "application/pdf",
                        "file_uri", fileUri
                )
        );
        Map<String, Object> textPart = Map.of("text", ANALYSIS_PROMPT);

        Map<String, Object> requestMap = Map.of(
                "contents", List.of(Map.of("parts", List.of(filePart, textPart)))
        );

        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(requestMap);
        } catch (JsonProcessingException e) {
            throw new LlmApiException("Failed to build Gemini request payload", e);
        }

        String urlWithKey = geminiProperties.apiUrl() + "?key=" + geminiProperties.apiKey();

        try {
            return restClient.post()
                    .uri(urlWithKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            log.error("Gemini generateContent call failed: {}", e.getMessage(), e);
            throw new LlmApiException("Failed to reach Gemini API: " + e.getMessage(), e);
        }
    }

    // ─── Parse response ────────────────────────────────────────────────────────

    /**
     * Parses the Gemini generateContent response.
     * Navigates to: candidates[0].content.parts[0].text → JSON string we prompted for.
     */
    private PdfAnalysisResponse parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode textNode = root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text");

            if (textNode.isMissingNode()) {
                log.error("Unexpected Gemini response structure: {}", responseBody);
                throw new LlmApiException("Unexpected response structure from Gemini API.");
            }

            String analysisJson = textNode.asText()
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("```", "")
                    .trim();

            log.debug("Extracted analysis JSON: {}", analysisJson);
            return objectMapper.readValue(analysisJson, PdfAnalysisResponse.class);

        } catch (LlmApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage(), e);
            throw new LlmApiException("Failed to parse analysis from Gemini API response.", e);
        }
    }
}
