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

// Common headers
const commonHeaders = {
  'Content-Type': 'application/json',
  'Origin': ORIGIN
};

test.describe("API Tests with Axios", () => {
  
  test("Verify agent authentication with axios", async () => {
    try {
      const response = await axios.post(
        `${API_URL}/auth/sign-in`,
        {
          email: process.env.AGENT_EMAIL,
          password: process.env.AGENT_PASSWORD
        },
        { headers: commonHeaders }
      );
      
      console.log('Status:', response.status);
      console.log('Data:', response.data);
      console.log('Headers:', response.headers);
      
      expect(response.status).toBe(200);
      expect(response.data.idToken).toBeTruthy();
      
    } catch (error: any) {
      console.log('Error status:', error.response?.status);
      console.log('Error data:', error.response?.data);
      console.log('Error headers:', error.response?.headers);
      throw error;
    }
  });
});