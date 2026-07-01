import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

/*
 * Vite-Konfiguration fuer den ENTWICKLUNGS-Server (`npm run dev`).
 *
 * Problem: Beim `npm run dev` laeuft KEIN Nginx. Das Frontend ruft aber
 * relative Adressen wie /api/auth/login auf. Damit diese trotzdem bei den
 * lokal laufenden Backends ankommen, leitet der Vite-Dev-Server sie hier
 * per "proxy" weiter (genau das macht im Docker-Betrieb sonst Nginx):
 *   /api/simulation/...  ->  backend_send    (Port 8084)
 *   /api/...             ->  backend_receive (Port 8083)
 *
 * Voraussetzung: die Backends muessen laufen, z. B.:
 *   ./mvnw -pl backend_receive spring-boot:run   (Login, Campus, Pinwand, Mensa)
 *   ./mvnw -pl backend_send    spring-boot:run   (nur fuer die Simulation noetig)
 */
export default defineConfig({
  plugins: [react()],
  server: {
    host: "0.0.0.0",
    port: 8090,
    proxy: {
      // Wichtig: der spezifischere Pfad zuerst.
      "/api/simulation": { target: "http://localhost:8084", changeOrigin: true },
      "/api": { target: "http://localhost:8083", changeOrigin: true }
    }
  }
});
