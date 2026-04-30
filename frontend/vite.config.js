import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import tailwindcss from "@tailwindcss/vite";

// Docker Compose overrides this so the frontend container can reach the backend service.
const apiProxyTarget = process.env.VITE_API_PROXY_TARGET ?? "http://localhost:8081";

export default defineConfig({
  plugins: [vue(), tailwindcss()],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: apiProxyTarget,
        changeOrigin: true,
        configure: (proxy, options) => {
          proxy.on('proxyRes', (proxyRes, req, res) => {
            const body = [];
            proxyRes.on('data', (chunk) => body.push(chunk));
            proxyRes.on('end', () => {
              res.removeHeader('transfer-encoding');
              res.removeHeader('Content-Length');
              res.end(Buffer.concat(body));
            });
          });
        }
      },
      "/health": {
        target: apiProxyTarget,
        changeOrigin: true
      }
    }
  }
});
