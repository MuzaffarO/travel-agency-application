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

async function getAuthToken(): Promise<string> {
    const response = await axios.post(
        `${API_URL}/auth/sign-in`,
        {
            email: process.env.AGENT_EMAIL,
            password: process.env.AGENT_PASSWORD
        },
        { headers: commonHeaders }
    );

    expect(response.status).toBe(200);
    expect(response.data.idToken).toBeTruthy();
    return response.data.idToken;
}

test.describe("Basic Smoke Tests", () => {

    test("1. Check API availability", async () => {
        const response = await axios.get(`${API_URL}/tours/available`, {
            headers: commonHeaders,
            validateStatus: null
        }).catch(error => {
            if (axios.isAxiosError(error)) {
                return error.response;
            }
            throw error;
        });

        expect(response?.status).toBe(200);
    });

    test("2. Verify agent authentication", async () => {
        const token = await getAuthToken();
        expect(token).toBeTruthy();
    });

    test("3. Get available tours list", async () => {
        const token = await getAuthToken();

        const response = await axios.get(
            `${API_URL}/tours/available`,
            {
                headers: {
                    ...commonHeaders,
                    'Authorization': `Bearer ${token}`
                }
            }
        );

        expect(response.status).toBe(200);
        expect(response.data).toHaveProperty('tours');
        expect(Array.isArray(response.data.tours)).toBe(true);
    });

    test("4. Verify tour details", async () => {
        const token = await getAuthToken();

        // First get the list of tours
        const toursResponse = await axios.get(
            `${API_URL}/tours/available`,
            {
                headers: {
                    ...commonHeaders,
                    'Authorization': `Bearer ${token}`
                }
            }
        );

        expect(toursResponse.data.tours.length).toBeGreaterThan(0);
        const firstTour = toursResponse.data.tours[0];

        // Then verify details of the first tour
        const detailsResponse = await axios.get(
            `${API_URL}/tours/${firstTour.id}`,
            {
                headers: {
                    ...commonHeaders,
                    'Authorization': `Bearer ${token}`
                }
            }
        );

        expect(detailsResponse.status).toBe(200);
        expect(detailsResponse.data).toHaveProperty('id');
        expect(detailsResponse.data.id).toBe(firstTour.id);
    });
});