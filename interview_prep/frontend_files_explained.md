# Frontend Files Explained

This document explains the purpose of every important file in the frontend (React + Vite), why it was written, and the key logic/methods inside it.

## 1. Components (The UI Layer)

### `HomePage.jsx`
*   **Why it exists:** This is the main page that glues everything together. It acts as the "Layout" for the app.
*   **Key Logic:** 
    *   It imports the `usePdfAnalysis` hook to get the state (`isLoading`, `result`, `error`).
    *   It uses a simple React `useState` (`activeTab`) to toggle between the URL Input form and the File Upload form.
    *   If `result` is populated by the hook, it hides the forms and renders the `<AnalysisResult>` component instead.

### `UrlInputForm.jsx` & `FileUploadForm.jsx`
*   **Why they exist:** To collect input from the user (either a URL string or a local PDF file).
*   **Key Logic:**
    *   Both components have internal state (e.g., `url` string or `selectedFile` object).
    *   When the user submits, they call `e.preventDefault()` to stop the browser from refreshing.
    *   They then trigger the `onSubmit(data)` prop, passing the data *up* to the `HomePage`, which then passes it to the hook.

### `AnalysisResult.jsx`
*   **Why it exists:** To display the JSON response from the backend in a beautiful, readable format.
*   **Key Logic:** It receives the `result` object as a prop and renders the title, authors, document type, summary, and key takeaways using CSS classes for styling.

## 2. State & Logic (The Hook Layer)

### `usePdfAnalysis.js`
*   **Why it exists:** This is a Custom React Hook. We use it to pull all the API fetching and loading state *out* of the UI components. This is a massive plus in interviews because it demonstrates "Separation of Concerns".
*   **Key Logic & Methods:**
    *   `handleAnalyse(url)` and `handleUpload(file)`: These methods are triggered by the UI. They immediately set `isLoading(true)` and clear any previous errors.
    *   They then await the result from the Service layer.
    *   `handleError(err)`: If the API fails, this reads the error message and sets it into the React state so the UI can display it.

## 3. The Service Layer

### `pdfAnalyserService.js`
*   **Why it exists:** To manage Axios (HTTP client) calls. By putting Axios here, components never have to import Axios directly. If we want to change authentication or headers later, we only change this one file.
*   **Key Logic & Methods:**
    *   `apiClient`: We create an Axios instance with a 90-second timeout, because LLMs (like Gemini) can sometimes take 30-40 seconds to read a massive PDF.
    *   `analyseUpload(file)`: This is crucial! To upload a file via HTTP, we cannot send standard JSON. We must create a `new FormData()` object, append the file to it, and set the header `Content-Type: multipart/form-data`. Axios then sends the raw file bytes to Spring Boot.

## 4. Configuration

### `apiConstants.js`
*   **Why it exists:** To centrally store API URLs.
*   **Key Logic:** We hardcode the backend URL (e.g., `https://finzly-pdf-anaylyser.onrender.com`) here. During local development, you'd point this to `http://localhost:8080`. Keeping this in one file means you don't have to hunt through components to change the URL when deploying.

### `HomePage.module.css` (and other CSS modules)
*   **Why they exist:** We use CSS Modules instead of standard CSS.
*   **Key Logic:** In a regular React app, if two files have `.button { color: red; }`, they conflict! CSS modules fix this. When Vite builds the project, it renames the classes to random strings (like `.button_axyz12`). This guarantees our CSS never overlaps or breaks across components.
