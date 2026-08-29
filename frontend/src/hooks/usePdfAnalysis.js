import { useState } from 'react';
import { analysePdf, analyseUpload } from '../services/pdfAnalyserService';

/**
 * Custom hook that encapsulates all state and logic for PDF analysis.
 *
 * Why a custom hook?
 *   - Separates concerns: UI components only render; hooks handle state + logic
 *   - Reusable — any component can use this hook
 *   - Testable in isolation without rendering any DOM
 *
 * Returns:
 *   - result: the structured analysis (or null before first analysis)
 *   - isLoading: true while the API call is in-flight
 *   - error: user-friendly error message (or null)
 *   - handleAnalyse: function to trigger analysis with a given URL
 *   - handleUpload: function to trigger analysis with a local file
 *   - reset: clears result and error (e.g. when user starts typing a new URL)
 */
const usePdfAnalysis = () => {
  const [result, setResult] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleError = (err) => {
    setError(err.message);
  };

  const handleAnalyse = async (pdfUrl) => {
    setIsLoading(true);
    setError(null);
    setResult(null);

    try {
      const data = await analysePdf(pdfUrl);
      setResult(data);
    } catch (err) {
      handleError(err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpload = async (file) => {
    setIsLoading(true);
    setError(null);
    setResult(null);

    try {
      const data = await analyseUpload(file);
      setResult(data);
    } catch (err) {
      handleError(err);
    } finally {
      setIsLoading(false);
    }
  };

  const reset = () => {
    setResult(null);
    setError(null);
  };

  return { result, isLoading, error, handleAnalyse, handleUpload, reset };
};

export default usePdfAnalysis;
