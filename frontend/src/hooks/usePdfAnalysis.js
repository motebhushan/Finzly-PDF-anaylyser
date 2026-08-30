import { useState } from 'react';
import { analysePdf, analyseUpload } from '../services/pdfAnalyserService';

/**
 * Custom hook to manage PDF analysis state and API calls.
 */
const usePdfAnalysis = () => {
  const [result, setResult] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const [lastAction, setLastAction] = useState(null);

  const handleError = (err) => {
    setError(err.message);
  };

  const handleAnalyse = async (pdfUrl) => {
    setLastAction({ type: 'url', payload: pdfUrl });
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
    setLastAction({ type: 'upload', payload: file });
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
    setLastAction(null);
  };

  const retry = () => {
    if (lastAction?.type === 'url') {
      handleAnalyse(lastAction.payload);
    } else if (lastAction?.type === 'upload') {
      handleUpload(lastAction.payload);
    } else {
      reset();
    }
  };

  return { result, isLoading, error, handleAnalyse, handleUpload, reset, retry };
};

export default usePdfAnalysis;
