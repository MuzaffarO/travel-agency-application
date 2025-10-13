import { expect, test } from "@playwright/test";
import { testUsers } from "../fixtures/testData";
import { LoginPage } from "../pages/LoginPage";
import { MainPage } from "../pages/MainPage";
import { MyToursPage } from "../pages/MyToursPage";
import { TEST_FILES, TIMEOUTS } from "../utils/constants";
import { AuthUtils } from "../utils/helpers";

test.describe("Bookings Management Tests", () => {
  let myToursPage: MyToursPage;
  let loginPage: LoginPage;
  let mainPage: MainPage;

  test.beforeEach(async ({ page }) => {
    myToursPage = new MyToursPage(page);
    loginPage = new LoginPage(page);
    mainPage = new MainPage(page);

    // Login before each test
    await AuthUtils.loginUser(
      page,
      testUsers.validCustomer.email,
      testUsers.validCustomer.password
    );
  });

  test.describe("My Tours Page Display", () => {
    test("TC-UI-056: My Tours - View Bookings", async ({ page }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.verifyMyToursPageElements();

      await myToursPage.waitForBookingsToLoad();
      await myToursPage.verifyBookingsDisplayed();
    });

    test("TC-UI-057: Booking Cards - Content Verification", async ({
      page,
    }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      const bookingCount = await myToursPage.getBookingCount();

      if (bookingCount > 0) {
        await myToursPage.verifyBookingCardElements(0);

        const bookingDetails = await myToursPage.getBookingDetails(0);
        expect(bookingDetails.name).toBeTruthy();
        expect(bookingDetails.status).toBeTruthy();
        expect(bookingDetails.date).toBeTruthy();
      } else {
        await myToursPage.verifyNoBookingsMessage();
      }
    });

    test("TC-UI-058: Tab Navigation - All Tours", async ({ page }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      // Test all tabs
      const tabs = [
        "All tours",
        "Booked",
        "Confirmed",
        "Started",
        "Finished",
        "Cancelled",
      ];

      for (const tab of tabs) {
        await myToursPage.switchToTab(tab as any);
        await myToursPage.waitForBookingsToLoad();

        const count = await myToursPage.getBookingCount();
        expect(count).toBeGreaterThanOrEqual(0);
      }
    });

    test("TC-UI-059: Loading State", async ({ page }) => {
      // Mock slow API response
      await page.route("**/api/bookings", async (route) => {
        await new Promise((resolve) => setTimeout(resolve, 2000));
        route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify({ bookings: [] }),
        });
      });

      await myToursPage.navigateToMyTours();

      // Verify loading state
      await myToursPage.verifyLoadingState();

      // Wait for loading to complete
      await myToursPage.waitForBookingsToLoad();
    });

    test("TC-UI-060: Error State Handling", async ({ page }) => {
      // Mock API error
      await page.route("**/api/bookings", (route) => {
        route.fulfill({
          status: 500,
          contentType: "application/json",
          body: JSON.stringify({ error: "Server error" }),
        });
      });

      await myToursPage.navigateToMyTours();

      // Verify error state
      await myToursPage.verifyErrorState();
    });
  });

  test.describe("Booking Actions", () => {
    test("TC-UI-061: Cancel Booking Flow", async ({ page }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      const bookingCount = await myToursPage.getBookingCount();

      if (bookingCount > 0) {
        const bookingDetails = await myToursPage.getBookingDetails(0);

        await myToursPage.cancelBooking(0);

        // Verify cancel modal content
        await myToursPage.verifyCancelModalContent(
          bookingDetails.name,
          bookingDetails.date
        );

        await myToursPage.confirmCancellation();

        // Verify booking status updated
        await myToursPage.verifyBookingStatus(0, "Cancelled");
      }
    });

    test("TC-UI-062: Cancel Modal - Close Without Action", async ({ page }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      const bookingCount = await myToursPage.getBookingCount();

      if (bookingCount > 0) {
        await myToursPage.cancelBooking(0);
        await myToursPage.closeCancelModal();

        // Verify modal is closed and booking unchanged
        const cancelModal = page.locator('[data-testid="cancel-modal"]');
        await expect(cancelModal).toBeHidden();
      }
    });

    test("TC-UI-063: Upload Documents Flow", async ({ page }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      const bookingCount = await myToursPage.getBookingCount();

      if (bookingCount > 0) {
        await myToursPage.uploadDocuments(0);

        await myToursPage.verifyUploadDocsModalContent();

        // Upload a test file
        await myToursPage.uploadFile(TEST_FILES.VALID_DOCUMENT);
        await myToursPage.submitDocumentUpload();

        // Verify success message
        const successMessage = page.locator(
          "text=Documents uploaded successfully"
        );
        await expect(successMessage).toBeVisible({ timeout: TIMEOUTS.MEDIUM });
      }
    });

    test("TC-UI-064: Upload Documents - Invalid File Type", async ({
      page,
    }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      const bookingCount = await myToursPage.getBookingCount();

      if (bookingCount > 0) {
        await myToursPage.uploadDocuments(0);

        // Try to upload invalid file
        await myToursPage.uploadFile(TEST_FILES.INVALID_FILE);

        // Verify error message
        const errorMessage = page.locator("text=Invalid file type");
        await expect(errorMessage).toBeVisible({ timeout: TIMEOUTS.MEDIUM });
      }
    });

    test("TC-UI-065: Send Review Flow", async ({ page }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      // Switch to finished tours tab
      await myToursPage.switchToTab("Finished");

      const finishedBookings = await myToursPage.getBookingCount();

      if (finishedBookings > 0) {
        await myToursPage.sendReview(0);

        await myToursPage.verifyFeedbackModalContent();

        await myToursPage.submitFeedback(
          5,
          "Excellent tour! Highly recommended."
        );

        // Verify success message
        const successMessage = page.locator("text=Thank you for your review");
        await expect(successMessage).toBeVisible({ timeout: TIMEOUTS.MEDIUM });
      }
    });

    test("TC-UI-066: Review Modal - Rating Validation", async ({ page }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.switchToTab("Finished");

      const finishedBookings = await myToursPage.getBookingCount();

      if (finishedBookings > 0) {
        await myToursPage.sendReview(0);

        // Try to submit without rating
        const submitButton = page.locator(
          '[data-testid="feedback-modal"] button:has-text("Submit")'
        );
        await submitButton.click();

        // Verify validation error
        const errorMessage = page.locator("text=Please select a rating");
        await expect(errorMessage).toBeVisible();
      }
    });
  });

  test.describe("Booking Status Management", () => {
    test("TC-UI-067: Status-Based Tab Filtering", async ({ page }) => {
      await myToursPage.navigateToMyTours();

      // Get counts for each status
      const allCount = await myToursPage.getBookingsByStatus("All tours");
      const bookedCount = await myToursPage.getBookingsByStatus("Booked");
      const confirmedCount = await myToursPage.getBookingsByStatus("Confirmed");
      const cancelledCount = await myToursPage.getBookingsByStatus("Cancelled");

      // Verify counts are logical
      expect(allCount).toBeGreaterThanOrEqual(
        bookedCount + confirmedCount + cancelledCount
      );
    });

    test("TC-UI-068: Booking Action Buttons by Status", async ({ page }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      const bookingCount = await myToursPage.getBookingCount();

      if (bookingCount > 0) {
        const bookingDetails = await myToursPage.getBookingDetails(0);

        // Verify action buttons based on status
        switch (bookingDetails.status) {
          case "Booked":
            await myToursPage.verifyBookingActionButtons(0, [
              "Cancel",
              "Upload Documents",
            ]);
            break;
          case "Confirmed":
            await myToursPage.verifyBookingActionButtons(0, [
              "Cancel",
              "Upload Documents",
            ]);
            break;
          case "Finished":
            await myToursPage.verifyBookingActionButtons(0, ["Send Review"]);
            break;
          case "Cancelled":
            // No action buttons for cancelled bookings
            break;
        }
      }
    });

    test("TC-UI-069: Status Update After Action", async ({ page }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      const bookingCount = await myToursPage.getBookingCount();

      if (bookingCount > 0) {
        const initialStatus = (await myToursPage.getBookingDetails(0)).status;

        if (initialStatus === "Booked" || initialStatus === "Confirmed") {
          await myToursPage.cancelBooking(0);
          await myToursPage.confirmCancellation();

          // Verify status changed to Cancelled
          await myToursPage.verifyBookingStatus(0, "Cancelled");
        }
      }
    });
  });

  test.describe("Responsive Design", () => {
    test("TC-UI-070: My Tours Mobile Layout", async ({ page }) => {
      await page.setViewportSize({ width: 375, height: 667 });

      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      // Verify page elements are accessible on mobile
      await myToursPage.verifyMyToursPageElements();

      // Test tab navigation on mobile
      await myToursPage.switchToTab("Booked");
      await myToursPage.switchToTab("Confirmed");
    });

    test("TC-UI-071: Booking Cards Mobile View", async ({ page }) => {
      await page.setViewportSize({ width: 375, height: 667 });

      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      const bookingCount = await myToursPage.getBookingCount();

      if (bookingCount > 0) {
        // Verify booking card elements are visible on mobile
        await myToursPage.verifyBookingCardElements(0);

        // Test action buttons on mobile
        const bookingDetails = await myToursPage.getBookingDetails(0);
        if (bookingDetails.status === "Booked") {
          await myToursPage.cancelBooking(0);
          await myToursPage.closeCancelModal();
        }
      }
    });

    test("TC-UI-072: Modal Responsiveness", async ({ page }) => {
      await page.setViewportSize({ width: 375, height: 667 });

      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      const bookingCount = await myToursPage.getBookingCount();

      if (bookingCount > 0) {
        await myToursPage.cancelBooking(0);

        // Verify modal is properly sized for mobile
        const modal = page.locator('[data-testid="cancel-modal"]');
        await expect(modal).toBeVisible();

        const modalWidth = await modal.evaluate(
          (el) => el.getBoundingClientRect().width
        );
        const viewportWidth = await page.evaluate(() => window.innerWidth);

        // Modal should not exceed viewport width
        expect(modalWidth).toBeLessThanOrEqual(viewportWidth);

        await myToursPage.closeCancelModal();
      }
    });
  });

  test.describe("Performance", () => {
    test("TC-UI-073: My Tours Page Load Performance", async ({ page }) => {
      const startTime = Date.now();
      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();
      const loadTime = Date.now() - startTime;

      expect(loadTime).toBeLessThan(5000); // Should load within 5 seconds
    });

    test("TC-UI-074: Tab Switching Performance", async ({ page }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      const startTime = Date.now();
      await myToursPage.switchToTab("Booked");
      await myToursPage.waitForBookingsToLoad();
      const switchTime = Date.now() - startTime;

      expect(switchTime).toBeLessThan(2000); // Tab switch should be fast
    });

    test("TC-UI-075: Document Upload Performance", async ({ page }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      const bookingCount = await myToursPage.getBookingCount();

      if (bookingCount > 0) {
        await myToursPage.uploadDocuments(0);

        const startTime = Date.now();
        await myToursPage.uploadFile(TEST_FILES.VALID_DOCUMENT);
        await myToursPage.submitDocumentUpload();
        const uploadTime = Date.now() - startTime;

        expect(uploadTime).toBeLessThan(10000); // Upload should complete within 10 seconds
      }
    });
  });

  test.describe("Accessibility", () => {
    test("TC-UI-076: Keyboard Navigation - Tab Controls", async ({ page }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      // Navigate tabs using keyboard
      await page.keyboard.press("Tab");

      // Find focused element
      const focusedElement = await page.evaluate(
        () => document.activeElement?.textContent
      );
      expect(focusedElement).toBeTruthy();
    });

    test("TC-UI-077: Screen Reader Support - Booking Information", async ({
      page,
    }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      const bookingCount = await myToursPage.getBookingCount();

      if (bookingCount > 0) {
        const bookingCard = page
          .locator('[data-testid="my-tour-card"]')
          .first();

        // Check for ARIA labels
        const ariaLabel = await bookingCard.getAttribute("aria-label");
        const ariaLabelledBy = await bookingCard.getAttribute(
          "aria-labelledby"
        );

        expect(ariaLabel || ariaLabelledBy).toBeTruthy();
      }
    });

    test("TC-UI-078: Modal Accessibility - Focus Management", async ({
      page,
    }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      const bookingCount = await myToursPage.getBookingCount();

      if (bookingCount > 0) {
        await myToursPage.cancelBooking(0);

        // Verify focus is trapped in modal
        const modal = page.locator('[data-testid="cancel-modal"]');
        await expect(modal).toBeVisible();

        // Check if modal has proper focus management
        const focusedElement = await page.evaluate(
          () => document.activeElement?.tagName
        );
        expect(focusedElement).toBeTruthy();

        // Test Escape key to close modal
        await page.keyboard.press("Escape");
        await expect(modal).toBeHidden();
      }
    });
  });

  test.describe("Error Handling", () => {
    test("TC-UI-079: Network Error During Cancellation", async ({ page }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      const bookingCount = await myToursPage.getBookingCount();

      if (bookingCount > 0) {
        // Mock network error for cancellation
        await page.route("**/api/bookings/*/cancel", (route) => {
          route.abort("failed");
        });

        await myToursPage.cancelBooking(0);
        await myToursPage.confirmCancellation();

        // Verify error handling
        const errorMessage = page.locator("text=Network error");
        await expect(errorMessage).toBeVisible({ timeout: TIMEOUTS.MEDIUM });
      }
    });

    test("TC-UI-080: File Upload Error Handling", async ({ page }) => {
      await myToursPage.navigateToMyTours();
      await myToursPage.waitForBookingsToLoad();

      const bookingCount = await myToursPage.getBookingCount();

      if (bookingCount > 0) {
        // Mock upload error
        await page.route("**/api/bookings/*/documents", (route) => {
          route.fulfill({
            status: 413,
            contentType: "application/json",
            body: JSON.stringify({ error: "File too large" }),
          });
        });

        await myToursPage.uploadDocuments(0);
        await myToursPage.uploadFile(TEST_FILES.LARGE_FILE);
        await myToursPage.submitDocumentUpload();

        // Verify error message
        const errorMessage = page.locator("text=File too large");
        await expect(errorMessage).toBeVisible({ timeout: TIMEOUTS.MEDIUM });
      }
    });
  });
});
