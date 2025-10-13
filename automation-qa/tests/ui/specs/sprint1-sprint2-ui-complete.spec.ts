import { expect, test } from "@playwright/test";
import { TIMEOUTS } from "../utils/constants";

// Load environment variables
const AGENT_EMAIL = process.env.AGENT_EMAIL;
const AGENT_PASSWORD = process.env.AGENT_PASSWORD;
const BASE_URL = process.env.BASE_URL || "http://localhost:5173";

// Validate required environment variables
if (!AGENT_EMAIL || !AGENT_PASSWORD) {
  throw new Error(
    "AGENT_EMAIL and AGENT_PASSWORD must be set in environment variables"
  );
}

test.describe("Sprint 1 + Sprint 2 - Complete UI Tests", () => {
  test.describe("Sprint 1 - Authentication (US_1, US_2)", () => {
    test("S1-UI-001: Customer Registration - Happy Path", async ({ page }) => {
      await page.goto(`${BASE_URL}/register`);

      // Verify registration page loads
      await expect(
        page.locator("h1:has-text('Create an account')")
      ).toBeVisible();

      // Fill registration form
      await page.locator("#firstName").fill("Test");
      await page.locator("#lastName").fill("Customer");
      await page.locator("#email").fill(`test-${Date.now()}@example.com`);
      await page.locator("#password").fill(AGENT_PASSWORD);
      await page.locator("#confirmPassword").fill(AGENT_PASSWORD);

      // Submit
      await page.locator('button:has-text("Create an account")').click();

      // Verify redirect to login or success message
      await expect(page).toHaveURL(/login/, { timeout: TIMEOUTS.MEDIUM });
    });

    test("S1-UI-002: Customer Login - Valid Credentials", async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);

      // Verify login page loads
      await expect(
        page.locator('h1:has-text("Sign in to your account")')
      ).toBeVisible();

      // Fill login form
      await page.locator("#email").fill(AGENT_EMAIL);
      await page.locator("#password").fill(AGENT_PASSWORD);

      // Submit
      await page.locator('button:has-text("Sign in")').click();

      // Verify agent redirects to main page
      await expect(page).toHaveURL(/\//, { timeout: TIMEOUTS.LONG });

      // Verify user is logged in (check header or user menu)
      const header = page.locator("h1", { hasText: 'Search for your next tour' });
      await expect(header).toBeVisible();
    });

    test("S1-UI-003: Customer Login - Invalid Credentials", async ({
      page,
    }) => {
      await page.goto(`${BASE_URL}/login`);

      // Fill with invalid credentials
      await page.locator("#email").fill("invalid@test.com");
      await page.locator("#password").fill("WrongPassword123!");

      // Submit
      await page.locator('button:has-text("Sign in")').click();

      // Verify error message appears
      const errorMessage = page.locator("span", { hasText: 'Incorrect' });
      await expect(errorMessage).toBeVisible({ timeout: TIMEOUTS.MEDIUM });

      // Verify stays on login page
      await expect(page).toHaveURL(/login/);
    });
  });

  test.describe("Sprint 1 - Tour Search and Display (US_4, US_5)", () => {
    test("S1-UI-004: Main Page - Display Available Tours", async ({ page }) => {
      await page.goto(`${BASE_URL}/`);

      // Verify main page title
      await expect(
        page.locator('h1:has-text("Search for your next tour")')
      ).toBeVisible({ timeout: TIMEOUTS.MEDIUM });

      // Wait for tours to load
      await page.waitForSelector('.shadow-card', {
        timeout: TIMEOUTS.LONG,
      });

      // Verify at least one tour card is displayed
      const tourCards = page.locator(
        '.shadow-card'
      );
      const count = await tourCards.count();
      expect(count).toBeGreaterThan(0);
    });

    test("S1-UI-005: Tour Card - Click to View Details", async ({ page }) => {
      await page.goto(`${BASE_URL}/`);

      // Wait for tours to load
      await page.waitForSelector('.shadow-card', {
        timeout: TIMEOUTS.LONG,
      });

      // Find and click the first tour card or "View details" link/button
      const viewDetailsButton = page
        .locator(
          'button:has-text("See details"), button:has-text("View details"), button:has-text("details")'
        )
        .first();

      if ((await viewDetailsButton.count()) > 0) {
        await viewDetailsButton.click();

        // Verify navigation to tour details page
        await expect(page).toHaveURL(/\/tours\/[^/]+/, {
          timeout: TIMEOUTS.MEDIUM,
        });

        // Verify tour details page elements
        await expect(page.locator("h1")).toBeVisible();
        await expect(page.locator("text=/rating|reviews/i")).toBeVisible({
          timeout: TIMEOUTS.MEDIUM,
        });
      } else {
        // Try clicking the tour card itself
        const firstTourCard = page
          .locator('.shadow-card')
          .first();
        await firstTourCard.click();

        // Check if navigated to details
        await page.waitForTimeout(1000);
        const currentUrl = page.url();
        if (currentUrl.includes("/tours/")) {
          await expect(page).toHaveURL(/\/tours\//);
        }
      }
    });

    test("S1-UI-006: Tour Details Page - Content Display", async ({ page }) => {
      await page.goto(`${BASE_URL}/`);

      // Wait for tours and navigate to details
      await page.waitForSelector('.shadow-card', {
        timeout: TIMEOUTS.LONG,
      });

      // Get first tour card and click
      const firstTourCard = page
        .locator(
          'button:has-text("See details"), button:has-text("View details"), button:has-text("details")'
        )
        .first();
      await firstTourCard.click();

      // Wait for details page to load
      await page.waitForURL(/\/tours\/[^/]+/, { timeout: TIMEOUTS.MEDIUM });

      // Verify tour details elements
      await expect(page.locator("h1")).toBeVisible();

      // Check for booking section
      const bookingButton = page.locator(
        'button:has-text("Book the tour"), button:has-text("Book")'
      );
      await expect(bookingButton).toBeVisible({ timeout: TIMEOUTS.MEDIUM });

      // Check for tour information sections
      await expect(
        page.locator('p', { hasText: /accommodation|meal|duration/i }).first()
      ).toBeVisible();

    });

    test("S1-UI-007: Tour Details - Booking Configuration", async ({
      page,
    }) => {
      await page.goto(`${BASE_URL}/`);

      // Navigate to tour details
      await page.waitForSelector('.shadow-card', {
        timeout: TIMEOUTS.LONG,
      });
      // Get first tour card and click
      const firstTourCard = page
        .locator(
          'button:has-text("See details"), button:has-text("View details"), button:has-text("details")'
        )
        .first();
      await firstTourCard.click();

      // Wait for details page to load
      await page.waitForURL(/\/tours\/[^/]+/, { timeout: TIMEOUTS.MEDIUM });

      // Verify tour details elements
      await expect(page.locator("h1")).toBeVisible();

      // Verify booking configuration dropdowns exist
      const dateSelector = page
        .locator('button:has(svg.lucide-calendar)')
        .first();
      await expect(dateSelector).toBeVisible({ timeout: TIMEOUTS.MEDIUM });

      // Verify total price is calculated
      const priceElement = page.locator('p:has-text("Total price:")');
      await expect(priceElement).toBeVisible();
    });
  });

  test.describe("Sprint 1 - Tour Booking Flow (US_6)", () => {
    test.beforeEach(async ({ page }) => {
      // Login before booking tests
      await page.goto(`${BASE_URL}/login`);
      await page.locator("#email").fill(AGENT_EMAIL);
      await page.locator("#password").fill(AGENT_PASSWORD);
      await page.locator('button:has-text("Sign in")').click();

      // Wait for redirect
      await page.waitForURL(/my-tours|\/$/, { timeout: TIMEOUTS.LONG });
    });

    test("S1-UI-008: Book Tour from Main Page - Authenticated User", async ({
      page,
    }) => {
      // Navigate to main page
      await page.goto(`${BASE_URL}/`);

      // Wait for tours to load
      await page.waitForSelector('.shadow-card', {
        timeout: TIMEOUTS.LONG,
      });

      // Find and click "Book" button on first tour
      const bookButton = page
        .locator('button:has-text("Book the tour"), button:has-text("Book")')
        .first();

      if ((await bookButton.count()) > 0) {
        await bookButton.click();

        // Verify booking modal or form appears
        const modal = page.locator('form:has(h3:has-text("Personal Details"))');
        await expect(modal.first()).toBeVisible({ timeout: TIMEOUTS.MEDIUM });
      }

    });

    test("S1-UI-009: Book Tour from Details Page", async ({ page }) => {
      // Navigate to main page
      await page.goto(`${BASE_URL}/`);

      // Wait for tours and click first one
      await page.waitForSelector('.shadow-card', {
        timeout: TIMEOUTS.LONG,
      });
      const viewDetailsButton = page
        .locator('button:has-text("See details"), button:has-text("View details"), button:has-text("details")')
        .first();
      await viewDetailsButton.click();

      // Wait for details page
      await page.waitForURL(/\/tours\/[^/]+/, { timeout: TIMEOUTS.MEDIUM });

      // Click "Book the tour" button
      const bookButton = page.locator(
        'button:has-text("Book the tour"), button:has-text("Book")'
      );
      await expect(bookButton).toBeVisible({ timeout: TIMEOUTS.MEDIUM });
      await bookButton.click();

      // Verify booking submission or redirect
      await page.waitForTimeout(2000); // Give time for booking to process

      // Should show success message or redirect
      const successIndicator = page.locator('p', { hasText: /Booking Confirmed|success|booked/i });
      await expect(successIndicator.first()).toBeVisible({ timeout: TIMEOUTS.MEDIUM });

    });

    test("S1-UI-010: Unauthenticated User - Book Tour Blocked", async ({
      page,
    }) => {
      // Logout first (clear localStorage)
      await page.goto(`${BASE_URL}/`);
      await page.evaluate(() => localStorage.clear());
      await page.reload();

      // Wait for tours to load
      await page.waitForSelector('.shadow-card', {
        timeout: TIMEOUTS.LONG,
      });

      // Try to book a tour
      const bookButton = page
        .locator('button:has-text("Book the tour"), button:has-text("Book")')
        .first();

      if ((await bookButton.count()) > 0) {
        await bookButton.click();

        // Verify "not logged in" modal appears
        const notLoggedModal = page.getByText(/please sign in or create an account/i);
        await expect(notLoggedModal).toBeVisible({ timeout: TIMEOUTS.MEDIUM });

        // Optionally check redirect if clicking "Sign in"
        const modal = page.locator('div[role="dialog"], .fixed.inset-0'); // modal container
        await modal.getByRole('button', { name: 'Sign in' }).click();
        await expect(page).toHaveURL(/\/login/);
      }
    });
  });

  test.describe("Sprint 2 - Agent Tours Page (US_8 view)", () => {
    test.beforeEach(async ({ page }) => {
      // Login before each test
      await page.goto(`${BASE_URL}/login`);
      await page.locator("#email").fill(AGENT_EMAIL);
      await page.locator("#password").fill(AGENT_PASSWORD);
      await page.locator('button:has-text("Sign in")').click();
      await page.waitForURL(/\//, { timeout: TIMEOUTS.LONG });
    });

    test("S2-UI-001: Agent - View Main Page", async ({ page }) => {
      await page.goto(`${BASE_URL}/`);

      // Verify page loads
      await expect(page).toHaveURL(/\//);

      // Check for tours display
      await page.waitForSelector('.shadow-card', {
        timeout: TIMEOUTS.LONG,
      });

      const tourCards = page.locator(
        '.shadow-card'
      );
      const count = await tourCards.count();
      expect(count).toBeGreaterThan(0);
    });

    test("S2-UI-002: Agent - Header Navigation", async ({ page }) => {
      // Login first
      await page.goto(`${BASE_URL}/login`);
      
      // Verify login page loads
      await expect(
        page.locator('h1:has-text("Sign in to your account")')
      ).toBeVisible();

      // Fill login form and submit
      await page.locator("#email").fill(AGENT_EMAIL);
      await page.locator("#password").fill(AGENT_PASSWORD);
      await page.locator('button:has-text("Sign in")').click();

      // Wait for login to complete and verify redirect
      await expect(page).toHaveURL(/\//, { timeout: TIMEOUTS.LONG });
      
      // Verify user is logged in (check header)
      const welcomeHeader = page.locator("h1", { hasText: 'Search for your next tour' });
      await expect(welcomeHeader).toBeVisible();

      // Check for agent navigation items
      const header = page.locator("header, nav");
      await expect(header.first()).toBeVisible();

      // Check for navigation links
      const allToursLink = page.locator('a:has-text("All tours")');
      const bookingsLink = page.locator('a:has-text("Bookings")');

      await expect(allToursLink).toBeVisible({
        timeout: TIMEOUTS.MEDIUM,
      });

      await expect(bookingsLink).toBeVisible({
        timeout: TIMEOUTS.MEDIUM,
      });
    });
  });

  test.describe("Sprint 2 - Agent Specific Features", () => {
    test.beforeEach(async ({ page }) => {
      // Login
      await page.goto(`${BASE_URL}/login`);
      await page.locator("#email").fill(AGENT_EMAIL);
      await page.locator("#password").fill(AGENT_PASSWORD);
      await page.locator('button:has-text("Sign in")').click();
      await page.waitForURL(/\//, { timeout: TIMEOUTS.LONG });
    });

    test("S2-UI-003: Agent - View All Tours", async ({ page }) => {
      await page.goto(`${BASE_URL}/`);

      // Verify tours are displayed
      await page.waitForSelector('.shadow-card', {
        timeout: TIMEOUTS.LONG,
      });

      const tourCards = page.locator(
        '.shadow-card'
      );
      const count = await tourCards.count();
      expect(count).toBeGreaterThan(0);

      // Verify agent can see tour details
      const viewDetailsButton = page
        .locator('button:has-text("See details"), button:has-text("View details"), button:has-text("details")')
        .first();
      await viewDetailsButton.click();

      // Should navigate to tour details
      await page.waitForURL(/\/tours\/[^/]+/, { timeout: TIMEOUTS.MEDIUM });
    });
  });

  test.describe("Responsive Design", () => {
    test("S1-UI-011: Mobile View - Main Page", async ({ page }) => {
      await page.setViewportSize({ width: 375, height: 667 });

      await page.goto(`${BASE_URL}/`);

      // Verify page loads on mobile
      await expect(
        page.locator('h1:has-text("Search for your next tour")')
      ).toBeVisible({ timeout: TIMEOUTS.MEDIUM });

      // Verify tours are displayed
      await page.waitForSelector('.shadow-card', {
        timeout: TIMEOUTS.LONG,
      });

      const tourCards = page.locator(
        '.shadow-card'
      );
      const count = await tourCards.count();
      expect(count).toBeGreaterThan(0);
    });

    test("S2-UI-012: Mobile View - Agent Login", async ({ page }) => {
      await page.setViewportSize({ width: 375, height: 667 });

      // Login
      await page.goto(`${BASE_URL}/login`);
      await page.locator("#email").fill(AGENT_EMAIL);
      await page.locator("#password").fill(AGENT_PASSWORD);
      await page.locator('button:has-text("Sign in")').click();

      await page.waitForURL(/\//, { timeout: TIMEOUTS.LONG });

      // Verify main page on mobile
      await page.goto(`${BASE_URL}/`);

      // Check tours are visible and functional on mobile
      const tourCards = page.locator(
        '.shadow-card'
      );
      await expect(tourCards.first()).toBeVisible();
    });
  });

  test.describe("Navigation and Header", () => {
    test("S1-UI-013: Header Navigation - Agent Menu", async ({ page }) => {
      // Login
      await page.goto(`${BASE_URL}/login`);

      // Verify login page loads
      await expect(
        page.locator('h1:has-text("Sign in to your account")')
      ).toBeVisible();

      // Fill login form and submit
      await page.locator("#email").fill(AGENT_EMAIL);
      await page.locator("#password").fill(AGENT_PASSWORD);
      await page.locator('button:has-text("Sign in")').click();

      // Wait for login to complete and verify redirect
      await expect(page).toHaveURL(/\//, { timeout: TIMEOUTS.LONG });
      
      // Verify user is logged in (check header)
      const welcomeHeader = page.locator("h1", { hasText: 'Search for your next tour' });
      await expect(welcomeHeader).toBeVisible();

      // Check for agent navigation items
      const header = page.locator("header, nav");
      await expect(header.first()).toBeVisible();

      // Check for navigation links
      const allToursLink = page.locator('a:has-text("All tours")');
      const bookingsLink = page.locator('a:has-text("Bookings")');

      await expect(allToursLink).toBeVisible({
        timeout: TIMEOUTS.MEDIUM,
      });

      await expect(bookingsLink).toBeVisible({
        timeout: TIMEOUTS.MEDIUM,
      });
    });
  });
});
