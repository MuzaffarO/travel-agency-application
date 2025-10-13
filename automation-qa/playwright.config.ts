import { defineConfig, devices } from "@playwright/test";
import * as dotenv from "dotenv";
import path from "path";

// Load test.env if it exists, otherwise fall back to test.env.example
const envPath = path.resolve(process.cwd(), "config/test.env");
const exampleEnvPath = path.resolve(process.cwd(), "config/test.env.example");

try {
  dotenv.config({ path: envPath });
} catch {
  dotenv.config({ path: exampleEnvPath });
}

const BASE_URL =
  process.env.BASE_URL || "https://travel-agency-dev.example.com";
const API_BASE_URL = process.env.API_BASE_URL || `${BASE_URL}/api`;

export default defineConfig({
  testDir: "./tests",
  outputDir: "test-results",
  reporter: [
    ["list"],
    ["junit", { outputFile: "test-results/junit.xml" }],
    ["html"],
  ],
  use: {
    baseURL: BASE_URL,
    trace: "retain-on-failure",
    video: "retain-on-failure",
    screenshot: "only-on-failure",
  },
  workers:4,
  projects: [
    {
      name: "smoke",
      testDir: "./tests/smoke",
      use: {
        baseURL: BASE_URL,
      },
    },
    {
      name: "API",
      testDir: "./tests/api",
      use: {
        extraHTTPHeaders: {
          "Content-Type": "application/json",
        },
        baseURL: API_BASE_URL,
      },
    },
    {
      name: "UI",
      testDir: "./tests/ui",
      use: {
        ...devices["Desktop Chrome"],
        baseURL: BASE_URL,
      },
    },
  ],
});
