# PDF Analyser

> AI-powered PDF analysis web application — paste a PDF URL, get structured insights instantly.

Built with **Spring Boot 4** (backend) + **React 18 + Vite** (frontend) + **Google Gemini 1.5 Flash** (LLM).

---

## Architecture

```
Browser (React/Vite)
  └── POST /api/v1/pdf/analyse
        └── Spring Boot Backend
              └── Google Gemini API (server-side only)
```

**Key security principle:** The Gemini API key lives exclusively in the backend environment — it is never exposed to the browser.

---

## Project Structure

```
pdf-analyser/
├── backend/         ← Spring Boot 4 (Java 21, Maven)
│   └── src/main/java/com/finzly/pdfanalyser/
│       ├── config/       CorsConfig, AppConfig, GeminiProperties
│       ├── controller/   PdfAnalyserController
│       ├── service/      PdfAnalyserService + Impl
│       ├── client/       GeminiClient + Impl
│       ├── dto/          Request/Response/Error DTOs
│       ├── exception/    Custom exceptions + GlobalExceptionHandler
│       └── util/         PdfUrlValidator
│
├── frontend/        ← React 18 + Vite
│   └── src/
│       ├── pages/        HomePage
│       ├── components/   UrlInputForm, AnalysisResult, LoadingSpinner, ErrorMessage
│       ├── hooks/        usePdfAnalysis
│       ├── services/     pdfAnalyserService (Axios)
│       └── constants/    apiConstants
│
├── .env.example     ← Required environment variables template
└── README.md
```

---

## Local Development Setup

### Prerequisites
- Java 21+
- Maven 3.9+
- Node.js 18+
- A Google Gemini API key (free at [aistudio.google.com](https://aistudio.google.com))

### 1. Backend

```bash
cd backend

# Set your API key
export GEMINI_API_KEY=your_api_key_here   # macOS/Linux
set GEMINI_API_KEY=your_api_key_here      # Windows

# Run the Spring Boot app
./mvnw spring-boot:run
# Backend starts on http://localhost:8080
```

### 2. Frontend

```bash
cd frontend

# Install dependencies
npm install

# Start dev server
npm run dev
# Frontend starts on http://localhost:5173
```

### 3. Test the API directly

```bash
curl -X POST http://localhost:8080/api/v1/pdf/analyse \
  -H "Content-Type: application/json" \
  -d '{"pdfUrl":"https://arxiv.org/pdf/1706.03762"}'
```

---

## API Reference

### `POST /api/v1/pdf/analyse`

**Request:**
```json
{ "pdfUrl": "https://arxiv.org/pdf/1706.03762" }
```

**Success Response `200 OK`:**
```json
{
  "documentType": "Research Paper",
  "title": "Attention Is All You Need",
  "authors": "Vaswani et al.",
  "summary": "This paper proposes the Transformer architecture...",
  "keyTakeaway": "Self-attention mechanisms alone can replace recurrence and convolutions."
}
```

**Error Response:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "The provided URL does not point to a valid PDF.",
  "timestamp": "2026-08-29T05:30:00Z"
}
```

---

## Deployment

### Backend → [Render](https://render.com)
1. Connect GitHub repo to Render
2. **Build command:** `cd backend && mvn clean package -DskipTests`
3. **Start command:** `java -jar backend/target/pdf-analyser-0.0.1-SNAPSHOT.jar`
4. Set environment variables in Render dashboard:
   - `GEMINI_API_KEY` = your Gemini API key
   - `CORS_ALLOWED_ORIGINS` = `https://your-app.vercel.app`
   - `SPRING_PROFILES_ACTIVE` = `prod`

### Frontend → [Vercel](https://vercel.com)
1. Connect GitHub repo to Vercel
2. **Root directory:** `frontend`
3. Set environment variable in Vercel dashboard:
   - `VITE_API_BASE_URL` = `https://your-backend.onrender.com`

---

## Running Tests

```bash
# Backend tests
cd backend
./mvnw test

# Frontend build verification
cd frontend
npm run build
```

---

## Environment Variables

| Variable | Where | Description |
|----------|-------|-------------|
| `GEMINI_API_KEY` | Backend | Google Gemini API key |
| `CORS_ALLOWED_ORIGINS` | Backend | Frontend origin URL (e.g. `https://app.vercel.app`) |
| `SPRING_PROFILES_ACTIVE` | Backend | `dev` or `prod` |
| `VITE_API_BASE_URL` | Frontend | Backend base URL |

Copy `.env.example` to `.env` and fill in values for local development.
