/**
 * API constants — single source of truth for all backend endpoint paths.
 * Hardcoded production backend URL to bypass Vercel environment variable issues
 */

export const API_BASE_URL = 'https://finzly-pdf-anaylyser.onrender.com/';

export const API_ENDPOINTS = {
  ANALYSE: '/api/v1/pdf/analyse',
  UPLOAD: '/api/v1/pdf/upload',
  HEALTH: '/api/v1/pdf/health'
};
