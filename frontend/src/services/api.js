import axios from 'axios';

// Local development uses Vite's /api proxy. Deployments set VITE_API_URL to
// the public API base URL; no environment-specific host is hardcoded here.
const apiBaseUrl = import.meta.env.VITE_API_URL?.trim() || '/api';

const api = axios.create({
  baseURL: apiBaseUrl,
  timeout: 12000,
});

api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('echo.token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (
      error.response?.status === 401 &&
      sessionStorage.getItem('echo.token') &&
      !window.location.pathname.startsWith('/login') &&
      !window.location.pathname.startsWith('/register')
    ) {
      sessionStorage.removeItem('echo.token');
      sessionStorage.removeItem('echo.user');
      if (window.location.pathname !== '/login') {
        window.location.assign('/login');
      }
    }
    return Promise.reject(error);
  }
);

export default api;

export function apiErrorMessage(error) {
  if (error.response) {
    return error.response.data?.message || `The server returned an error (${error.response.status}). Please try again.`;
  }
  if (error.code === 'ECONNABORTED') {
    return import.meta.env.DEV
      ? 'The server took too long to respond. Check that the backend and database are running.'
      : 'The request timed out. Please try again in a few moments.';
  }
  if (import.meta.env.DEV) {
    return 'Echo API is not responding. Start MySQL, then run start-echo.cmd from the project folder and retry.';
  }
  return 'Unable to connect to the Echo service. Please check your network connection and try again.';
}
