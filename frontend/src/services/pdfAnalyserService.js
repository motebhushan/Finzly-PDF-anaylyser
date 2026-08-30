import axios from 'axios';
import { API_BASE_URL, API_ENDPOINTS } from '../constants/apiConstants';

/**
 * Service layer for PDF analysis API calls.
 */
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 90000, // 90s — LLM calls can be slow
});

/**
 * Sends a PDF URL to the backend for LLM analysis.
 *
 * @param {string} pdfUrl - Publicly accessible PDF URL
 * @returns {Promise<{documentType, title, authors, summary, keyTakeaway}>}
 * @throws {Error} with a user-friendly message on failure
 */
export const analysePdf = async (pdfUrl) => {
  try {
    const response = await apiClient.post(API_ENDPOINTS.ANALYSE, { pdfUrl });
    return response.data;
  } catch (error) {
    const message =
      error.response?.data?.message ||
      error.message ||
      'An unexpected error occurred. Please try again.';
    throw new Error(message);
  }
};

/**
 * Sends a PDF file to the backend for LLM analysis.
 *
 * @param {File} file - PDF file to upload
 * @returns {Promise<{documentType, title, authors, summary, keyTakeaway}>}
 * @throws {Error} with a user-friendly message on failure
 */
export const analyseUpload = async (file) => {
  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await apiClient.post(API_ENDPOINTS.UPLOAD, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  } catch (error) {
    const message =
      error.response?.data?.message ||
      error.message ||
      'An unexpected error occurred during upload. Please try again.';
    throw new Error(message);
  }
};
