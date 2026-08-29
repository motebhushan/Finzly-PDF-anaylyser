/**
 * API constants — single source of truth for all backend endpoint paths.
 * Change the base URL here when deploying; nothing else needs to change.
 */

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const API_ENDPOINTS = {
  ANALYSE: '/api/v1/pdf/analyse',
  UPLOAD: '/api/v1/pdf/upload',
  HEALTH: '/api/v1/pdf/health'
};
