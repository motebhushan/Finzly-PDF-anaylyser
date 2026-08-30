# Frontend Interview Preparation (React + Vite)

## 1. The Architecture & Flow
The frontend is built using **React 18** and **Vite** (for fast bundling). It strictly follows the **Container-Presenter Pattern** and separates UI rendering from business logic using Custom Hooks.

**The Flow of a Request:**
1.  **User Input:** The user either enters a URL in `UrlInputForm.jsx` or uploads a file in `FileUploadForm.jsx`.
2.  **Trigger Analysis:** Both components trigger a function passed down via props (`onSubmit={handleAnalyse}` or `onSubmit={handleUpload}`).
3.  **The Custom Hook (`usePdfAnalysis.js`):** 
    *   This is the "brain" of the frontend. It intercepts the call and sets `isLoading = true`.
    *   It then calls the actual API function inside the Service layer.
4.  **The Service Layer (`pdfAnalyserService.js`):**
    *   This layer uses `axios` to make the actual HTTP `POST` requests to the backend (`/api/v1/pdf/analyse` or `/api/v1/pdf/upload`).
    *   It manages the base URL (from `apiConstants.js`) and handles the multipart form data construction.
5.  **State Update:** Once the promise resolves, the Service returns data back to the Hook. The Hook sets `result = data` and `isLoading = false`.
6.  **UI Re-render:** The `HomePage.jsx` component reacts to the state changes from the Hook. If `result` is populated, it renders the `AnalysisResult.jsx` component instead of the input forms.

## 2. Key Technical Decisions
*   **Why extract logic into `usePdfAnalysis.js`?**
    *   It keeps the UI components purely focused on rendering HTML/CSS. If we decide to add Redux or React Query later, we only need to change this one hook, and none of the visual components will break.
*   **Why extract API calls into `pdfAnalyserService.js`?**
    *   By abstracting `axios` into a service file, we prevent `axios` imports from being scattered across dozens of components. It makes it incredibly easy to switch base URLs (Dev vs. Prod) and handle global timeouts.
*   **How do you handle CSS?**
    *   We use **CSS Modules** (e.g., `HomePage.module.css`). This prevents CSS class name collisions because Vite automatically scopes the class names (e.g., `.title` becomes `._title_1x8as_5`).

---

## 3. Potential Q&A for the Interviewer

**Q: How did you implement the File Upload feature?**
A: I used an HTML `<input type="file">` restricted to `accept="application/pdf"`. When the form is submitted, the file is passed to `pdfAnalyserService.js`, which appends it to a JavaScript `FormData` object. We then send it via Axios with the `Content-Type: multipart/form-data` header so the backend can read the raw bytes.

**Q: How do you handle errors on the frontend?**
A: The backend returns standardized JSON error responses (like 400 Bad Request or 500 Internal Server Error). Our Axios catch block in the `usePdfAnalysis` hook reads `error.response.data.message` and sets the React `error` state. The `HomePage` then conditionally renders the `ErrorMessage` component to display the friendly string to the user.

**Q: Why use Vite instead of Create React App (CRA)?**
A: Create React App is officially deprecated. Vite uses native ES modules during development, which means it starts the dev server almost instantly and features lightning-fast Hot Module Replacement (HMR). It then uses Rollup for highly optimized production builds.

**Q: How did you handle environment variables?**
A: We created an `apiConstants.js` file. Locally, we used Vite's `import.meta.env.VITE_API_BASE_URL` to point to `localhost:8080`. For production, because Vercel exposes `VITE_` variables to the browser, we hardcoded the production URL to bypass complex build-step configurations and ensure absolute stability.
