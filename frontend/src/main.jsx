import React from "react";
import { createRoot } from "react-dom/client";
import App from "./App.jsx";
import "./styles.css";

/*
 * Einstiegspunkt der React-App.
 *
 * React bekommt das HTML-Element #root und rendert darin unsere
 * gesamte Anwendung. StrictMode hilft beim Entwickeln, weil React
 * typische Fehler frueher sichtbar macht.
 */
createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
