import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

/*
 * Vite-Konfiguration fuer den Entwicklungsserver (`npm run dev`).
 *
 * Ohne Nginx uebernimmt der Dev-Server das Weiterleiten der /api-Aufrufe an die
 * lokal laufenden Backends. Diese muessen dafuer gestartet sein.
 */
export default defineConfig({
  plugins: [react()],
  server: {
    host: "0.0.0.0",
    port: 8090,
    proxy: {
      // Der spezifischere Pfad muss zuerst stehen.
      "/api/simulation": { target: "http://localhost:8084", changeOrigin: true },
      "/api": { target: "http://localhost:8083", changeOrigin: true }
    }
  }
});
