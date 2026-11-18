import { defineConfig } from 'vite';

const backendUrl = process.env.ECHO_BACKEND_URL || 'http://127.0.0.1:8081';

export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: backendUrl,
        changeOrigin: true,
      },
    },
  },
});
