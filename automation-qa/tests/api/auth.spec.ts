import { expect, test } from "@playwright/test";
import { createMockApi } from "../mocks/mockApi";

const uniqueEmail = () => `user_${Date.now()}@example.com`;
const USE_MOCKS = process.env.USE_MOCKS === "true";

test.describe("Auth API", () => {
  test("register success", async ({ request }) => {
    if (USE_MOCKS) {
      const mock = createMockApi();
      const res = await mock.post("/auth/sign-up", {
        data: {
          email: uniqueEmail(),
          password: "Password123!",
          firstName: "John",
          lastName: "Doe",
        },
      });
      expect([201, 409]).toContain(res.status());
      return;
    }
    const res = await request.post("/auth/sign-up", {
      data: {
        email: uniqueEmail(),
        password: "Password123!",
        firstName: "John",
        lastName: "Doe",
      },
    });
    expect([201, 409]).toContain(res.status());
  });

  test("login success", async ({ request }) => {
    if (USE_MOCKS) {
      const mock = createMockApi();
      const res = await mock.post("/auth/sign-in", {
        data: {
          email: process.env.CUSTOMER_EMAIL || "customer@test.com",
          password: process.env.CUSTOMER_PASSWORD || "Password123!",
        },
      });
      expect(res.status()).toBe(200);
      const body = await res.json();
      expect(body.idToken).toBeTruthy();
      expect(body.role).toBeTruthy();
      return;
    }
    const res = await request.post("/auth/sign-in", {
      data: {
        email: process.env.CUSTOMER_EMAIL,
        password: process.env.CUSTOMER_PASSWORD,
      },
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.idToken).toBeTruthy();
    expect(body.role).toBeTruthy();
  });

  test("login invalid credentials", async ({ request }) => {
    if (USE_MOCKS) {
      const mock = createMockApi();
      const res = await mock.post("/auth/sign-in", {
        data: { email: "customer@test.com", password: "wrong-pass" },
      });
      expect(res.status()).toBe(400);
      return;
    }
    const res = await request.post("/auth/sign-in", {
      data: { email: "customer@test.com", password: "wrong-pass" },
    });
    expect(res.status()).toBe(400);
  });
});
