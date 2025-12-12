// src/config.ts

// This reads the env variable injected by Docker build.
// If not found (e.g., running locally without docker), it defaults to localhost.
export const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

// You can add other global configs here later (e.g. timeout settings)
export const APP_NAME = "Chronos Scheduler";