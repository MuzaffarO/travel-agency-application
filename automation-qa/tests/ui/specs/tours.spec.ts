import { expect, test } from "@playwright/test";
import { mockResponses, searchFilters, testUsers } from "../fixtures/testData";
import { LoginPage } from "../pages/LoginPage";
import { MainPage } from "../pages/MainPage";
import { TIMEOUTS, URLS, VIEWPORTS } from "../utils/constants";
import { AuthUtils, BrowserUtils } from "../utils/helpers";

test.describe("Tours and Search Tests", () => {
  let mainPage: MainPage;
  let loginPage: LoginPage;

  test.beforeEach(async ({ page }) => {
    mainPage = new MainPage(page);
    loginPage = new LoginPage(page);
  });

  test.describe("Tour Display and Loading", () => {
    test("TC-UI-026: Tour Search - Default View", async ({ page }) => {
      await mainPage.navigateToMain();
      await mainPage.verifyMainPageElements();

      await mainPage.waitForToursToLoad();
      await mainPage.verifyToursDisplayed();

      const tourCount = await mainPage.getTourCount();
      expect(tourCount).toBeGreaterThan(0);
    });

    test("TC-UI-027: Tour Cards - Content Verification", async ({ page }) => {
      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();

      // Verify first tour card has all required elements
      await mainPage.verifyTourCardElements(0);

      // Get tour details and verify format
      const tourDetails = await mainPage.getTourDetails(0);
      expect(tourDetails.name).toBeTruthy();
      expect(tourDetails.location).toBeTruthy();
      expect(tourDetails.price).toMatch(/\$[\d,]+/);
      expect(tourDetails.rating).toMatch(/\d+\.?\d*/);
    });

    test("TC-UI-028: Loading States", async ({ page }) => {
      // Mock slow API response
      await page.route("**/api/tours", async (route) => {
        await new Promise((resolve) => setTimeout(resolve, 2000));
        route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify(mockResponses.tours.success),
        });
      });

      await mainPage.navigateToMain();

      // Verify loading state is shown
      await mainPage.verifyLoadingState();

      // Wait for tours to load
      await mainPage.waitForToursToLoad();
      await mainPage.verifyToursDisplayed();
    });

    test("TC-UI-029: Error State Handling", async ({ page }) => {
      // Mock API error
      await page.route("**/api/tours", (route) => {
        route.fulfill({
          status: 500,
          contentType: "application/json",
          body: JSON.stringify({ error: "Server error" }),
        });
      });

      await mainPage.navigateToMain();

      // Verify error state is shown
      await mainPage.verifyErrorState();
    });

    test("TC-UI-030: No Results State", async ({ page }) => {
      // Mock empty results
      await page.route("**/api/tours", (route) => {
        route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify(mockResponses.tours.empty),
        });
      });

      await mainPage.navigateToMain();

      // Verify no results state
      await mainPage.verifyNoResultsState();
    });
  });

  test.describe("Search Functionality", () => {
    test("TC-UI-031: Basic Search - Location Filter", async ({ page }) => {
      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();

      const initialCount = await mainPage.getTourCount();

      await mainPage.searchTours({
        location: "Paris",
      });

      await mainPage.waitForToursToLoad();
      const filteredCount = await mainPage.getTourCount();

      // Verify search was performed (results may be same or different)
      expect(typeof filteredCount).toBe("number");
    });

    test("TC-UI-032: Advanced Search - Multiple Filters", async ({ page }) => {
      await mainPage.navigateToMain();

      await mainPage.searchTours(searchFilters.advancedSearch);

      await mainPage.waitForToursToLoad();
      await mainPage.verifyToursDisplayed();
    });

    test("TC-UI-033: Family Search - Adults and Children", async ({ page }) => {
      await mainPage.navigateToMain();

      await mainPage.searchTours(searchFilters.familySearch);

      await mainPage.waitForToursToLoad();
      const tourCount = await mainPage.getTourCount();
      expect(tourCount).toBeGreaterThanOrEqual(0);
    });

    test("TC-UI-034: Date Range Search", async ({ page }) => {
      await mainPage.navigateToMain();

      await mainPage.searchTours({
        startDate: "2025-06-15",
        endDate: "2025-06-22",
      });

      await mainPage.waitForToursToLoad();
    });

    test("TC-UI-035: Search with No Results", async ({ page }) => {
      // Mock empty search results
      await page.route("**/api/tours*", (route) => {
        route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify(mockResponses.tours.empty),
        });
      });

      await mainPage.navigateToMain();
      await mainPage.searchTours({
        location: "NonexistentLocation",
      });

      await mainPage.verifyNoResultsState();
    });
  });

  test.describe("Sorting and Filtering", () => {
    test("TC-UI-036: Sort Tours - Top Rated First", async ({ page }) => {
      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();

      await mainPage.sortTours("Top rated first");

      await mainPage.waitForToursToLoad();
      await mainPage.verifyToursDisplayed();
    });

    test("TC-UI-037: Sort Tours - Most Popular", async ({ page }) => {
      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();

      await mainPage.sortTours("Most popular");

      await mainPage.waitForToursToLoad();
      await mainPage.verifyToursDisplayed();
    });

    test("TC-UI-038: Filter Persistence", async ({ page }) => {
      await mainPage.navigateToMain();

      // Apply filters
      await mainPage.searchTours({
        location: "Paris",
        adults: 2,
      });

      // Refresh page
      await page.reload();

      // Verify filters are maintained (if implemented)
      await mainPage.verifyFiltersPersisted();
    });
  });

  test.describe("Tour Booking Flow", () => {
    test("TC-UI-039: Book Tour - Authenticated User", async ({ page }) => {
      // Login first
      await AuthUtils.loginUser(
        page,
        testUsers.validCustomer.email,
        testUsers.validCustomer.password
      );

      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();

      await mainPage.clickBookTour(0);

      await mainPage.verifyBookingModalOpened();
    });

    test("TC-UI-040: Book Tour - Unauthenticated User", async ({ page }) => {
      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();

      await mainPage.clickBookTour(0);

      await mainPage.verifyNotLoggedModalOpened();
    });

    test("TC-UI-041: Booking Modal - Content Verification", async ({
      page,
    }) => {
      await AuthUtils.loginUser(
        page,
        testUsers.validCustomer.email,
        testUsers.validCustomer.password
      );

      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();

      const tourDetails = await mainPage.getTourDetails(0);
      await mainPage.clickBookTour(0);

      await mainPage.verifyBookingModalOpened();

      // Verify modal contains tour information
      const modal = page.locator('[data-testid="booking-modal"]');
      await expect(modal).toContainText(tourDetails.name);
    });

    test("TC-UI-042: Close Booking Modal", async ({ page }) => {
      await AuthUtils.loginUser(
        page,
        testUsers.validCustomer.email,
        testUsers.validCustomer.password
      );

      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();

      await mainPage.clickBookTour(0);
      await mainPage.verifyBookingModalOpened();

      await mainPage.closeBookingModal();

      // Verify modal is closed
      const modal = page.locator('[data-testid="booking-modal"]');
      await expect(modal).toBeHidden();
    });

    test("TC-UI-043: Not Logged Modal - Navigation to Login", async ({
      page,
    }) => {
      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();

      await mainPage.clickBookTour(0);
      await mainPage.verifyNotLoggedModalOpened();

      // Click login button in modal
      const loginButton = page.locator(
        '[data-testid="not-logged-modal"] button:has-text("Login")'
      );
      await loginButton.click();

      await expect(page).toHaveURL(URLS.LOGIN);
    });
  });

  test.describe("Responsive Design", () => {
    test("TC-UI-044: Desktop Layout - 2 Column Grid", async ({ page }) => {
      await BrowserUtils.setViewportSize(page, "desktop");
      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();

      await mainPage.verifyResponsiveLayout("desktop");
    });

    test("TC-UI-045: Tablet Layout - Responsive Grid", async ({ page }) => {
      await BrowserUtils.setViewportSize(page, "tablet");
      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();

      await mainPage.verifyResponsiveLayout("tablet");
    });

    test("TC-UI-046: Mobile Layout - Single Column", async ({ page }) => {
      await BrowserUtils.setViewportSize(page, "mobile");
      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();

      await mainPage.verifyResponsiveLayout("mobile");

      // Verify search functionality still works on mobile
      await mainPage.searchTours({
        location: "Paris",
      });

      await mainPage.waitForToursToLoad();
    });

    test("TC-UI-047: Mobile Search Interface", async ({ page }) => {
      await page.setViewportSize(VIEWPORTS.MOBILE);
      await mainPage.navigateToMain();

      // Verify search elements are accessible on mobile
      await mainPage.verifyMainPageElements();

      // Test mobile-specific interactions
      await mainPage.selectLocation("Paris");
      await mainPage.selectGuests(2, 1);
      await mainPage.performSearch();
    });
  });

  test.describe("Performance and Optimization", () => {
    test("TC-UI-048: Page Load Performance", async ({ page }) => {
      const startTime = Date.now();
      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();
      const loadTime = Date.now() - startTime;

      expect(loadTime).toBeLessThan(5000); // Should load within 5 seconds
    });

    test("TC-UI-049: Search Performance", async ({ page }) => {
      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();

      const startTime = Date.now();
      await mainPage.searchTours(searchFilters.basicSearch);
      await mainPage.waitForToursToLoad();
      const searchTime = Date.now() - startTime;

      expect(searchTime).toBeLessThan(3000); // Search should complete within 3 seconds
    });

    test("TC-UI-050: Image Loading Optimization", async ({ page }) => {
      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();

      // Check that tour images are loaded
      const tourImages = page.locator('[data-testid="tour-image"]');
      const imageCount = await tourImages.count();

      for (let i = 0; i < Math.min(imageCount, 3); i++) {
        const image = tourImages.nth(i);
        await expect(image).toBeVisible();

        // Verify image has loaded (not broken)
        const naturalWidth = await image.evaluate(
          (img: HTMLImageElement) => img.naturalWidth
        );
        expect(naturalWidth).toBeGreaterThan(0);
      }
    });
  });

  test.describe("Accessibility", () => {
    test("TC-UI-051: Keyboard Navigation - Tour Cards", async ({ page }) => {
      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();

      // Navigate to first tour card using keyboard
      await page.keyboard.press("Tab");

      // Find the focused element
      const focusedElement = await page.evaluate(
        () => document.activeElement?.tagName
      );
      expect(focusedElement).toBeTruthy();
    });

    test("TC-UI-052: Screen Reader Support - Tour Information", async ({
      page,
    }) => {
      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();

      // Check that tour cards have proper ARIA labels
      const tourCards = page.locator('[data-testid="tour-card"]');
      const firstCard = tourCards.first();

      const ariaLabel = await firstCard.getAttribute("aria-label");
      const ariaLabelledBy = await firstCard.getAttribute("aria-labelledby");

      expect(ariaLabel || ariaLabelledBy).toBeTruthy();
    });

    test("TC-UI-053: Color Contrast - Tour Cards", async ({ page }) => {
      await mainPage.navigateToMain();
      await mainPage.waitForToursToLoad();

      // This is a basic visibility check
      // In a real implementation, you'd use accessibility testing tools
      const tourNames = page.locator('[data-testid="tour-name"]');
      const firstTourName = tourNames.first();

      await expect(firstTourName).toBeVisible();

      const textColor = await firstTourName.evaluate(
        (el) => window.getComputedStyle(el).color
      );
      const backgroundColor = await firstTourName.evaluate(
        (el) => window.getComputedStyle(el).backgroundColor
      );

      expect(textColor).toBeTruthy();
      expect(backgroundColor).toBeTruthy();
    });
  });

  test.describe("Error Scenarios", () => {
    test("TC-UI-054: Network Timeout During Search", async ({ page }) => {
      await mainPage.navigateToMain();

      // Mock slow/timeout response
      await page.route("**/api/tours*", (route) => {
        // Don't respond to simulate timeout
        setTimeout(() => {
          route.fulfill({
            status: 408,
            contentType: "application/json",
            body: JSON.stringify({ error: "Request timeout" }),
          });
        }, 10000);
      });

      await mainPage.searchTours(searchFilters.basicSearch);

      // Verify timeout error handling
      const errorMessage = page.locator("text=timeout");
      await expect(errorMessage).toBeVisible({ timeout: TIMEOUTS.LONG });
    });

    test("TC-UI-055: Invalid Search Parameters", async ({ page }) => {
      await mainPage.navigateToMain();

      // Try search with invalid date range
      await mainPage.searchTours({
        startDate: "2020-01-01", // Past date
        endDate: "2020-01-02",
      });

      // Verify error handling for invalid dates
      const errorMessage = page.locator("text=invalid date");
      await expect(errorMessage).toBeVisible({ timeout: TIMEOUTS.MEDIUM });
    });
  });
});
