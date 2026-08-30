# Backend Interview Preparation (Spring Boot)

## 1. The Architecture & Flow
The backend uses a standard **3-Tier Clean Architecture**. This ensures that HTTP mapping, business logic, and external API calls are kept strictly separate.

**The Flow of a Request:**
1.  **Client (React)** sends a `multipart/form-data` (file upload) or `application/json` (URL) POST request to the backend.
2.  **`PdfAnalyserController`** receives the request.
    *   **Method used:** `@PostMapping("/upload")` or `@PostMapping("/analyse")`.
    *   **Role:** It validates the input format, passes it to the Service layer, and wraps the response in a `ResponseEntity.ok()`. It contains *no business logic*.
3.  **`PdfAnalyserServiceImpl`** (The Service Layer) takes over.
    *   **Methods used:** `analyseUpload(byte[] pdfBytes, String originalFilename)` or `analyse(PdfAnalyseRequest request)`.
    *   **Role:** It performs business validation (e.g., checking if the PDF is under 20MB, or if a URL is reachable using `PdfUrlValidator.java`). It then delegates the actual AI work to the Client layer.
4.  **`GeminiClientImpl`** (The Client Layer) communicates with Google.
    *   **Methods used:** `uploadToGeminiFiles(byte[] pdfBytes)` and `callGenerateContent(String fileUri)`.
    *   **Role:** This uses Spring's `RestClient` to make HTTP calls to Google's API. First, it uploads the PDF bytes to the Gemini Files API, then calls the LLM with the prompt and the file URI. Finally, it uses Jackson's `ObjectMapper` to parse the raw JSON string returned by Gemini into our strongly-typed `PdfAnalysisResponse` object.
5.  **Return:** The `PdfAnalysisResponse` flows all the way back up to the Controller and is sent to the frontend.

## 2. Key Technical Decisions
*   **Why use `RestClient` instead of `RestTemplate` or `WebClient`?**
    *   `RestClient` is the modern, fluent HTTP client introduced in Spring Framework 6.1. It is synchronous (easier to read than WebClient) but offers a much cleaner builder API than the old `RestTemplate`.
*   **Why use `GlobalExceptionHandler`?**
    *   Instead of littering the Controller with `try-catch` blocks, a class annotated with `@RestControllerAdvice` intercepts any exceptions (like `InvalidPdfUrlException` or `LlmApiException`) and translates them into a standard `ApiErrorResponse` JSON. This ensures the frontend always gets a predictable error format.
*   **Why use the Gemini Files API instead of passing the PDF inline?**
    *   PDFs can be large. Passing massive base64 strings directly in the prompt payload causes token limit errors and payload size rejections. Uploading it first via the Files API is the industry-standard way to handle large documents in LLMs.

---

## 3. Potential Q&A for the Interviewer

**Q: How do you handle configuration and secrets?**
A: We use `application.yml` and `application-dev.yml` to store configurations like CORS settings and model names. The actual API key is injected via an environment variable (`GEMINI_API_KEY`) so it is never hardcoded in the source code. We use a `@ConfigurationProperties` record (`GeminiProperties.java`) to strongly type these properties.

**Q: How do you handle CORS?**
A: The frontend and backend run on different ports (or different domains in production). I created a `CorsConfig.java` class implementing `WebMvcConfigurer`. It reads the allowed origins from the `application.yml` properties and explicitly allows `GET`, `POST`, and `OPTIONS` requests from the frontend domain.

**Q: What happens if the Gemini API fails or times out?**
A: If `RestClient` throws a `RestClientException`, the `GeminiClientImpl` catches it and throws a custom `LlmApiException`. The `GlobalExceptionHandler` intercepts this and returns an HTTP 500 or 503 response with a clear error message to the frontend, preventing the application from crashing.

**Q: How did you parse the JSON response from Gemini?**
A: Gemini returns a deeply nested JSON structure. I used Jackson's `ObjectMapper.readTree()` to navigate the JSON tree down to `candidates[0].content.parts[0].text`. The prompt explicitly asks Gemini to return *only* a JSON string, which I then map directly into the `PdfAnalysisResponse` Java record.
