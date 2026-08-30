# Backend Files Explained

This document explains the purpose of every important file in the backend (Spring Boot), why it was written, and the key logic/methods inside it.

## 1. Controller Layer

### `PdfAnalyserController.java`
*   **Why it exists:** This is the entry point for frontend HTTP requests. The browser talks directly to this file.
*   **Key Logic & Methods:**
    *   `@RestController`: Tells Spring this class handles web requests and returns JSON.
    *   `analyse(@RequestBody PdfAnalyseRequest request)`: Handles the URL input. It receives the JSON, passes the URL to the Service layer, and returns the response.
    *   `upload(@RequestParam("file") MultipartFile file)`: Handles the local PDF file upload. It reads the raw bytes of the file and passes them to the Service layer.
*   **Interview Tip:** Emphasize that controllers should be "thin". Notice there is zero business logic here, only routing!

## 2. Service Layer

### `PdfAnalyserServiceImpl.java`
*   **Why it exists:** This is the "brain" of the backend. It sits between the Controller (HTTP routing) and the Client (Google API) to handle business rules.
*   **Key Logic & Methods:**
    *   `analyse(PdfAnalyseRequest request)`: First, it calls `pdfUrlValidator.validate()` to make sure the URL is a real PDF. Then, it calls `geminiClient.analyse(pdfUrl)` to get the AI analysis.
    *   `analyseUpload(byte[] pdfBytes, String originalFilename)`: Checks if the uploaded file is empty or too large (> 20MB) before sending the bytes to Gemini.
*   **Interview Tip:** This layer ensures we don't send garbage data to Google, saving API costs and preventing crashes.

## 3. Client Layer

### `GeminiClientImpl.java`
*   **Why it exists:** To abstract away the complexity of communicating with Google's Gemini API. The Service layer just says "analyze this", and this file figures out *how* to do it.
*   **Key Logic & Methods:**
    *   `downloadPdf(String pdfUrl)`: If given a URL, it uses Java's native `HttpClient` to download the PDF into a `byte[]` array.
    *   `uploadToGeminiFiles(byte[] pdfBytes)`: Gemini 3.5+ requires large PDFs to be uploaded first. This method creates a `multipart/related` HTTP request using Spring's `RestClient` and uploads the PDF to Google, receiving a `fileUri` in return.
    *   `callGenerateContent(String fileUri)`: Sends the AI prompt and the `fileUri` to Gemini to generate the summary.
    *   `parseResponse(String responseBody)`: Gemini returns a massive, complex JSON tree. This method uses Jackson's `ObjectMapper.readTree()` to dig down into `candidates[0].content.parts[0].text` to extract the JSON string we actually care about.

## 4. Configuration & Utility

### `AppConfig.java` & `CorsConfig.java`
*   **Why they exist:** To setup Spring Boot beans. 
*   **Key Logic:** `CorsConfig` reads allowed origins (like your Vercel URL) from `application.yml` and tells Spring Boot to allow the frontend to bypass browser CORS security checks.

### `PdfUrlValidator.java`
*   **Why it exists:** To ensure the user gave us a real PDF URL.
*   **Key Logic:** It checks if the URL starts with `http` or `https`, and uses an HTTP `HEAD` request to check if the server returns a `Content-Type: application/pdf` header, all without downloading the entire file first!

## 5. Exception Handling

### `GlobalExceptionHandler.java`
*   **Why it exists:** To catch errors anywhere in the app and return a friendly JSON response instead of a scary Java stack trace.
*   **Key Logic:** Uses `@ExceptionHandler`. If the Gemini API fails, it catches `LlmApiException` and returns a 500/503 status code with a clean error message.

## 6. DTOs (Data Transfer Objects)
*   **Why they exist:** Files like `PdfAnalyseRequest.java` and `PdfAnalysisResponse.java` define the exact JSON shape of the data moving in and out of the API. We use Java `record` classes because they are immutable and lightweight.
