import { expect, test } from "@playwright/test";
import axios from 'axios';
import dotenv from 'dotenv';
import path from 'path';

// Load environment variables from test.env
dotenv.config({ path: path.resolve(__dirname, '../../config/test.env') });

// Validate required environment variables
const requiredEnvVars = ['API_BASE_URL', 'BASE_URL', 'AGENT_EMAIL', 'AGENT_PASSWORD'];
for (const envVar of requiredEnvVars) {
  if (!process.env[envVar]) {
    throw new Error(`Environment variable ${envVar} is not defined. Please check test.env file`);
  }
}

// API Configuration
const API_URL = process.env.API_BASE_URL;
const ORIGIN = process.env.BASE_URL;

// Utilities
const futureDate = () =>
  new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10);

const generateRandomEmail = () => `smoke_${Date.now()}@example.com`;

// Common headers
const commonHeaders = {
  'Content-Type': 'application/json',
  'Origin': ORIGIN
};

async function login(): Promise<string> {
  console.log('Attempting login with:', {
    email: process.env.AGENT_EMAIL,
    password: "***"
  });

  try {
    const response = await axios.post(
      `${API_URL}/auth/sign-in`,
      {
        email: process.env.AGENT_EMAIL,
        password: process.env.AGENT_PASSWORD
      },
      { headers: commonHeaders }
    );

    console.log('Login status:', response.status);
    expect(response.status).toBe(200);
    expect(response.data.idToken).toBeTruthy();
    return response.data.idToken;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      console.error('Login error:', {
        status: error.response.status,
        data: error.response.data
      });
    }
    throw error;
  }
}

test.describe("Smoke", () => {
  test("user authentication @smoke", async () => {
    // 1. First perform login
    const token = await login();
    expect(token).toBeTruthy();
  });

  test("list and view tour details @smoke", async () => {
    const token = await login();

    try {
      // 1. Get list of available tours
      const toursResponse = await axios.get(
        `${API_URL}/tours/available`,
        {
          headers: {
            ...commonHeaders,
            'Authorization': `Bearer ${token}`
          }
        }
      );

      expect(toursResponse.status).toBe(200);
      expect(Array.isArray(toursResponse.data.tours)).toBeTruthy();
      expect(toursResponse.data.tours.length).toBeGreaterThan(0);

      // 2. Get first tour details
      const tourId = toursResponse.data.tours[0].id;
      const detailsResponse = await axios.get(
        `${API_URL}/tours/${tourId}`,
        {
          headers: {
            ...commonHeaders,
            'Authorization': `Bearer ${token}`
          }
        }
      );
      
      expect(detailsResponse.status).toBe(200);
      expect(detailsResponse.data).toHaveProperty('id', tourId);

    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        console.error('Tours API error:', {
          status: error.response.status,
          data: error.response.data
        });
      }
      throw error;
    }
  });

  test("search destinations @smoke", async () => {
    try {
      const response = await axios.get(
        `${API_URL}/tours/destinations`,
        {
          headers: commonHeaders,
          params: {
            destination: "Dominican Republic"  // Minimum 3 characters
          }
        }
      );
      
      expect(response.status).toBe(200);
      expect(Array.isArray(response.data.destinations)).toBeTruthy();
      
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        console.error('Destinations API error:', {
          status: error.response.status,
          data: error.response.data
        });
      }
      throw error;
    }
  });
});
