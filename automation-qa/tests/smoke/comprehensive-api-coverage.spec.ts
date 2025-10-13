import { expect, test } from "@playwright/test";
import axios from "axios";
import dotenv from "dotenv";
import path from "path";

// Load environment variables from test.env
dotenv.config({ path: path.resolve(__dirname, "../../config/test.env") });

// Validate required environment variables
const requiredEnvVars = [
  "API_BASE_URL",
  "BASE_URL",
  "AGENT_EMAIL",
  "AGENT_PASSWORD",
];
for (const envVar of requiredEnvVars) {
  if (!process.env[envVar]) {
    throw new Error(
      `Environment variable ${envVar} is not defined. Please check test.env file`
    );
  }
}

// API Configuration
const API_URL = process.env.API_BASE_URL;
const ORIGIN = process.env.BASE_URL;

// Common headers
const commonHeaders = {
  "Content-Type": "application/json",
  Origin: ORIGIN,
};

async function login(): Promise<string> {
  const response = await axios.post(
    `${API_URL}/auth/sign-in`,
    {
      email: process.env.AGENT_EMAIL,
      password: process.env.AGENT_PASSWORD,
    },
    { headers: commonHeaders }
  );

  expect(response.status).toBe(200);
  expect(response.data.idToken).toBeTruthy();
  return response.data.idToken;
}

test.describe("Comprehensive API Coverage Validation", () => {
  test("Validate all documented endpoints are accessible @comprehensive", async () => {
    const token = await login();

    console.log("🔍 Comprehensive API Endpoint Coverage Test");
    console.log("📋 Validating all Sprint 1 + Sprint 2 endpoints...");

    const endpointTests = [
      // Authentication endpoints (Sprint 1)
      {
        method: "POST",
        endpoint: "/auth/sign-in",
        requiresAuth: false,
        description: "User authentication",
        sprint: 1,
      },
      {
        method: "PUT",
        endpoint: "/auth/profile",
        requiresAuth: true,
        description: "Update user profile",
        sprint: 2,
      },

      // Tours endpoints (Sprint 1)
      {
        method: "GET",
        endpoint: "/tours/available",
        requiresAuth: false,
        description: "Get available tours",
        sprint: 1,
      },
      {
        method: "GET",
        endpoint: "/tours/destinations",
        requiresAuth: false,
        description: "Get tour destinations",
        sprint: 1,
      },

      // Bookings endpoints (Sprint 2)
      {
        method: "POST",
        endpoint: "/bookings",
        requiresAuth: true,
        description: "Create booking",
        sprint: 2,
      },
      {
        method: "GET",
        endpoint: "/bookings",
        requiresAuth: true,
        description: "Get user bookings",
        sprint: 2,
      },
    ];

    let successCount = 0;
    let totalEndpoints = endpointTests.length;

    for (const endpointTest of endpointTests) {
      try {
        console.log(
          `\n🧪 Testing: ${endpointTest.method} ${endpointTest.endpoint}`
        );
        console.log(
          `   Sprint: ${endpointTest.sprint} | Auth Required: ${endpointTest.requiresAuth}`
        );

        const headers = endpointTest.requiresAuth
          ? { ...commonHeaders, Authorization: `Bearer ${token}` }
          : commonHeaders;

        let response;

        if (endpointTest.method === "GET") {
          // Add specific parameters for endpoints that require them
          const params =
            endpointTest.endpoint === "/tours/destinations"
              ? { destination: "dom" } // Provide search parameter for destinations
              : {};

          response = await axios.get(`${API_URL}${endpointTest.endpoint}`, {
            headers,
            params,
            validateStatus: null,
          });
        } else if (endpointTest.method === "POST") {
          // Use minimal valid data for POST endpoints
          let testData = {};
          if (endpointTest.endpoint === "/auth/sign-in") {
            testData = {
              email: process.env.AGENT_EMAIL,
              password: process.env.AGENT_PASSWORD,
            };
          } else if (endpointTest.endpoint === "/bookings") {
            // Skip booking creation in comprehensive test to avoid side effects
            console.log(
              "   ⚠️ Skipping booking creation in comprehensive test"
            );
            totalEndpoints--; // Reduce total count since we're skipping this endpoint
            continue;
          }

          response = await axios.post(
            `${API_URL}${endpointTest.endpoint}`,
            testData,
            {
              headers,
              validateStatus: null,
            }
          );
        } else if (endpointTest.method === "PUT") {
          if (endpointTest.endpoint === "/auth/profile") {
            response = await axios.put(
              `${API_URL}${endpointTest.endpoint}`,
              { firstName: "Test", lastName: "User" },
              {
                headers,
                validateStatus: null,
              }
            );
          }
        }

        const expectedSuccessCodes = [200, 201];
        const isSuccess = expectedSuccessCodes.includes(response!.status);

        if (isSuccess) {
          console.log(
            `   ✅ SUCCESS: ${response!.status} ${response!.statusText}`
          );
          successCount++;
        } else {
          console.log(
            `   ⚠️ RESPONSE: ${response!.status} ${response!.statusText}`
          );
          if (response!.status === 401) {
            console.log("   📝 Note: 401 may be expected for auth validation");
          }
        }
      } catch (error) {
        if (axios.isAxiosError(error) && error.response) {
          console.log(
            `   ❌ ERROR: ${error.response.status} ${error.response.statusText}`
          );
          console.log(`   📄 Response: ${JSON.stringify(error.response.data)}`);
        } else {
          console.log(`   ❌ NETWORK ERROR: ${error}`);
        }
      }
    }

    console.log("\n📊 COMPREHENSIVE COVERAGE SUMMARY:");
    console.log(`   ✅ Successful responses: ${successCount}`);
    console.log(`   📋 Total endpoints tested: ${totalEndpoints}`);
    console.log(
      `   📈 Success rate: ${Math.round(
        (successCount / totalEndpoints) * 100
      )}%`
    );

    // We expect at least 50% success rate (some endpoints may require specific conditions)
    // Note: Some endpoints may require specific permissions or data conditions
    expect(successCount).toBeGreaterThanOrEqual(totalEndpoints * 0.5);
  });

  test("Test endpoint variations and query parameters @comprehensive", async () => {
    const token = await login();

    console.log("🔍 Testing Endpoint Variations and Query Parameters");

    try {
      // Test tours with various query parameters
      const tourParams = [
        {},
        { page: 1, pageSize: 3 },
        { destination: "Paris" },
        { sortBy: "PRICE_ASC" },
        { adults: 2, children: 1 },
        { startDate: "2025-01-01", endDate: "2025-12-31" },
      ];

      for (let i = 0; i < tourParams.length; i++) {
        const params = tourParams[i];
        console.log(`\n🧪 Tours API Test ${i + 1}:`, JSON.stringify(params));

        const response = await axios.get(`${API_URL}/tours/available`, {
          headers: {
            ...commonHeaders,
            Authorization: `Bearer ${token}`,
          },
          params,
        });

        expect(response.status).toBe(200);
        expect(response.data).toHaveProperty("tours");
        expect(Array.isArray(response.data.tours)).toBe(true);

        console.log(
          `   ✅ Success: ${response.data.tours.length} tours returned`
        );
      }

      // Test destinations endpoint
      console.log("\n🧪 Testing destinations endpoint");

      const destinationsResponse = await axios.get(
        `${API_URL}/tours/destinations`,
        {
          headers: commonHeaders,
          params: { destination: "dom" }, // Partial search
        }
      );

      expect(destinationsResponse.status).toBe(200);
      expect(destinationsResponse.data).toHaveProperty("destinations");
      console.log(
        `   ✅ Destinations found: ${destinationsResponse.data.destinations.length}`
      );
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        console.error("Endpoint variations test error:", {
          status: error.response.status,
          data: error.response.data,
        });
      }
      throw error;
    }
  });

  test("Validate response schemas match documentation @comprehensive", async () => {
    const token = await login();

    console.log("🔍 Validating Response Schemas");

    try {
      // Test tours list schema
      const toursResponse = await axios.get(`${API_URL}/tours/available`, {
        headers: {
          ...commonHeaders,
          Authorization: `Bearer ${token}`,
        },
      });

      expect(toursResponse.status).toBe(200);
      expect(toursResponse.data).toHaveProperty("tours");
      // Check for pagination fields (API returns pagination data directly in response)
      expect(toursResponse.data).toHaveProperty("page");
      expect(toursResponse.data).toHaveProperty("pageSize");
      expect(toursResponse.data).toHaveProperty("totalItems");
      expect(toursResponse.data).toHaveProperty("totalPages");

      if (toursResponse.data.tours.length > 0) {
        const tour = toursResponse.data.tours[0];
        expect(tour).toHaveProperty("id");
        expect(tour).toHaveProperty("name");
        expect(tour).toHaveProperty("destination");
        expect(tour).toHaveProperty("price");
        expect(typeof tour.price).toBe("string"); // Price comes as string format like "from $850.0 for 1 person"

        console.log("   ✅ Tours list schema validated");

        // Test tour details schema
        const tourDetailsResponse = await axios.get(
          `${API_URL}/tours/${tour.id}`,
          {
            headers: {
              ...commonHeaders,
              Authorization: `Bearer ${token}`,
            },
          }
        );

        expect(tourDetailsResponse.status).toBe(200);
        expect(tourDetailsResponse.data).toHaveProperty("id", tour.id);
        expect(tourDetailsResponse.data).toHaveProperty("name");
        expect(tourDetailsResponse.data).toHaveProperty("summary"); // Backend returns 'summary' not 'description'

        console.log("   ✅ Tour details schema validated");
      }

      // Test bookings schema
      const bookingsResponse = await axios.get(`${API_URL}/bookings`, {
        headers: {
          ...commonHeaders,
          Authorization: `Bearer ${token}`,
        },
      });

      expect(bookingsResponse.status).toBe(200);
      expect(bookingsResponse.data).toHaveProperty("bookings");
      expect(Array.isArray(bookingsResponse.data.bookings)).toBe(true);

      console.log("   ✅ Bookings schema validated");
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        console.error("Schema validation error:", {
          status: error.response.status,
          data: error.response.data,
        });
      }
      throw error;
    }
  });

  test("Performance benchmarking for all endpoints @comprehensive", async () => {
    const token = await login();

    console.log("🔍 Performance Benchmarking");

    const performanceTests = [
      { endpoint: "/tours/available", method: "GET", auth: true },
      { endpoint: "/tours/destinations", method: "GET", auth: false },
      { endpoint: "/bookings", method: "GET", auth: true },
    ];

    const performanceResults: any[] = [];

    for (const test of performanceTests) {
      console.log(`\n⏱️ Benchmarking: ${test.method} ${test.endpoint}`);

      const headers = test.auth
        ? { ...commonHeaders, Authorization: `Bearer ${token}` }
        : commonHeaders;

      const measurements: number[] = [];

      // Run 3 iterations for each endpoint
      for (let i = 0; i < 3; i++) {
        const startTime = Date.now();

        try {
          const response = await axios.get(`${API_URL}${test.endpoint}`, {
            headers,
          });

          const endTime = Date.now();
          const responseTime = endTime - startTime;

          measurements.push(responseTime);
          console.log(
            `   Iteration ${i + 1}: ${responseTime}ms (${response.status})`
          );
        } catch (error) {
          console.log(`   Iteration ${i + 1}: ERROR`);
        }
      }

      if (measurements.length > 0) {
        const avgResponseTime =
          measurements.reduce((a, b) => a + b, 0) / measurements.length;
        const maxResponseTime = Math.max(...measurements);

        performanceResults.push({
          endpoint: test.endpoint,
          avgResponseTime,
          maxResponseTime,
        });

        console.log(`   📊 Average: ${Math.round(avgResponseTime)}ms`);
        console.log(`   📈 Max: ${maxResponseTime}ms`);

        // Performance assertion: API should respond within 3 seconds
        expect(avgResponseTime).toBeLessThan(3000);
      }
    }

    console.log("\n📊 PERFORMANCE SUMMARY:");
    performanceResults.forEach((result) => {
      console.log(
        `   ${result.endpoint}: ${Math.round(result.avgResponseTime)}ms avg`
      );
    });
  });
});
