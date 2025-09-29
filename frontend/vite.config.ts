import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: {
            '@': '/src'
        }
    },
    server: {
        port: 5173,
        open: false,
        proxy: {
            '/api': { target: 'http://localhost:8080', changeOrigin: true },
            '/products': { target: 'http://localhost:8080', changeOrigin: true },
            '/orders': { target: 'http://localhost:8080', changeOrigin: true },
            '/auth': { target: 'http://localhost:8080', changeOrigin: true }
        }
    },
    preview: {
        port: 5173
    }
});


