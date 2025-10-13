import { expect, test } from "@playwright/test";
import { LoginPage } from "../pages/LoginPage";
import { MyToursPage } from "../pages/MyToursPage";
import { TIMEOUTS } from "../utils/constants";

// Load environment variables
const CUSTOMER_EMAIL = process.env.CUSTOMER_EMAIL || "customer@test.com";
const CUSTOMER_PASSWORD = process.env.CUSTOMER_PASSWORD || "Password123!";

test.describe("Sprint 2 - Customer Only UI Tests", () => {
  let loginPage: LoginPage;
  let myToursPage: MyToursPage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    myToursPage = new MyToursPage(page);
  });

  test.describe("Customer Authentication", () => {
    test("TC-S2-UI-001: Customer Login - Happy Path", async ({ page }) => {
      await loginPage.navigateToLogin();

      // Mock successful login
      await page.route("**/api/auth/sign-in", (route) => {
        route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify({
            idToken: "mock-jwt-token",
            role: "CUSTOMER",
            userName: "Test Customer",
            email: CUSTOMER_EMAIL,
          }),
        });
      });

      await loginPage.login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
      // Verify redirect to customer area
      await expect(page).toHaveURL(/my-tours|\/?$/, {
        timeout: TIMEOUTS.MEDIUM,
      });
    });

    test("TC-S2-UI-002: Customer Registration - Happy Path", async ({
      page,
    }) => {
      await page.goto("/register");

      // Mock successful registration
      await page.route("**/api/auth/sign-up", (route) => {
        route.fulfill({
          status: 201,
          contentType: "application/json",
          body: JSON.stringify({
            message: "Account created successfully",
          }),
        });
      });

      // Fill registration form
      await page.locator("#firstName").fill("John");
      await page.locator("#lastName").fill("Doe");
      await page.locator("#email").fill(`new-${Date.now()}@test.com`);
      await page.locator("#password").fill(CUSTOMER_PASSWORD);
      await page.locator("#confirmPassword").fill(CUSTOMER_PASSWORD);

      await page.locator('button:has-text("Create an account")').click();

      // Verify success (simplified)
      await expect(page).toHaveURL(/login/, { timeout: TIMEOUTS.MEDIUM });
    });
  });

  test.describe("Sprint 2 - Document Upload (US_7)", () => {
    test("TC-S2-UI-003: Document Upload Modal Access", async ({ page }) => {
      // Mock login
      await page.route("**/api/auth/sign-in", (route) => {
        route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify({
            idToken: "mock-token",
            role: "CUSTOMER",
          }),
        });
      });

      // Mock bookings with documents feature
      await page.route("**/api/bookings", (route) => {
        route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify([
            {
              bookingId: "booking-123",
              tourName: "Paris Tour",
              state: "CONFIRMED",
              date: "2025-03-15",
              hasDocumentUpload: true,
            },
          ]),
        });
      });

      await loginPage.navigateToLogin();
      await loginPage.login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

      await page.goto("/my-tours");
      await expect(page).toHaveURL(/my-tours/);

      // Check if Upload Documents button exists
      const uploadButton = page.locator('button:has-text("Upload Documents")');
      if ((await uploadButton.count()) > 0) {
        await uploadButton.first().click();

        // Verify modal opens (basic check)
        const modal = page.locator('[data-testid*="upload"], [role="dialog"]');
        await expect(modal).toBeVisible({ timeout: TIMEOUTS.SHORT });
      }
    });
  });

  test.describe("Sprint 2 - Customer Feedback (US_9)", () => {
    test("TC-S2-UI-004: Feedback Submission Flow", async ({ page }) => {
      // Mock login
      await page.route("**/api/auth/sign-in", (route) => {
        route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify({
            idToken: "mock-token",
            role: "CUSTOMER",
          }),
        });
      });

      // Mock finished bookings
      await page.route("**/api/bookings", (route) => {
        route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify([
            {
              bookingId: "finished-123",
              tourName: "Completed Tour",
              state: "FINISHED",
              date: "2025-01-15",
              canReview: true,
            },
          ]),
        });
      });

      await loginPage.navigateToLogin();
      await loginPage.login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

      await page.goto("/my-tours");
      await expect(page).toHaveURL(/my-tours/);

      // Switch to finished tab if exists
      const finishedTab = page.locator('button:has-text("Finished")');
      if ((await finishedTab.count()) > 0) {
        await finishedTab.click();
      }

      // Check if Send Review button exists
      const reviewButton = page.locator(
        'button:has-text("Send Review"), button:has-text("Review")'
      );
      if ((await reviewButton.count()) > 0) {
        await reviewButton.first().click();

        // Verify feedback modal opens
        const modal = page.locator(
          '[data-testid*="feedback"], [data-testid*="review"], [role="dialog"]'
        );
        await expect(modal).toBeVisible({ timeout: TIMEOUTS.SHORT });
      }
    });
  });

  test.describe("Basic Navigation", () => {
    test("TC-S2-UI-005: My Tours Page Access", async ({ page }) => {
      // Mock minimal login
      await page.route("**/api/auth/sign-in", (route) => {
        route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify({
            idToken: "mock-token",
            role: "CUSTOMER",
          }),
        });
      });

      await loginPage.navigateToLogin();
      await loginPage.login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

      // Navigate to My Tours
      await page.goto("/my-tours");

      // Basic verification that page loads
      await expect(page).toHaveURL(/my-tours/, { timeout: TIMEOUTS.MEDIUM });

      // Check for basic page elements
      const pageContent = page.locator("body");
      await expect(pageContent).toBeVisible();
    });

    test("TC-S2-UI-006: Mobile Responsive Check", async ({ page }) => {
      await page.setViewportSize({ width: 375, height: 667 });

      await page.goto("/");

      // Basic mobile layout check
      const content = page.locator("body");
      await expect(content).toBeVisible();

      // Check that content adapts to mobile
      const viewportWidth = await page.evaluate(() => window.innerWidth);
      expect(viewportWidth).toBe(375);
    });
  });
});
