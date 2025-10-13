import { Locator, Page } from "@playwright/test";
import { BasePage } from "./BasePage";

export class MyToursPage extends BasePage {
  readonly pageTitle: Locator;
  readonly tourTabs: Locator;
  readonly allToursTab: Locator;
  readonly bookedTab: Locator;
  readonly confirmedTab: Locator;
  readonly startedTab: Locator;
  readonly finishedTab: Locator;
  readonly cancelledTab: Locator;
  readonly tourCards: Locator;
  readonly loadingMessage: Locator;
  readonly pageErrorMessage: Locator;
  readonly noBookingsMessage: Locator;
  readonly cancelModal: Locator;
  readonly uploadDocsModal: Locator;
  readonly feedbackModal: Locator;

  constructor(page: Page) {
    super(page);
    this.pageTitle = page.locator("h1"); // Adjust selector based on actual page title
    this.tourTabs = page.locator('[data-testid="tour-tabs"]');
    this.allToursTab = page.locator('button:has-text("All tours")');
    this.bookedTab = page.locator('button:has-text("Booked")');
    this.confirmedTab = page.locator('button:has-text("Confirmed")');
    this.startedTab = page.locator('button:has-text("Started")');
    this.finishedTab = page.locator('button:has-text("Finished")');
    this.cancelledTab = page.locator('button:has-text("Cancelled")');
    this.tourCards = page.locator('[data-testid="my-tour-card"]');
    this.loadingMessage = page.locator("text=Loading...");
    this.pageErrorMessage = page.locator("text=Error:");
    this.noBookingsMessage = page.locator("text=No bookings yet");
    this.cancelModal = page.locator('[data-testid="cancel-modal"]');
    this.uploadDocsModal = page.locator('[data-testid="upload-docs-modal"]');
    this.feedbackModal = page.locator('[data-testid="feedback-modal"]');
  }

  async navigateToMyTours(): Promise<void> {
    await this.navigateTo("/my-tours");
    await this.waitForPageLoad();
  }

  async waitForBookingsToLoad(): Promise<void> {
    // Wait for loading to disappear
    await this.page.waitForFunction(
      () => {
        const loading = document.querySelector("text=Loading...");
        return !loading;
      },
      { timeout: 15000 }
    );
  }

  async verifyMyToursPageElements(): Promise<void> {
    await this.verifyElementVisible(this.tourTabs);
    await this.verifyElementVisible(this.allToursTab);
  }

  async verifyBookingsDisplayed(): Promise<void> {
    await this.waitForBookingsToLoad();
    const bookingCount = await this.tourCards.count();
    if (bookingCount === 0) {
      await this.verifyElementVisible(this.noBookingsMessage);
    } else {
      await this.verifyElementVisible(this.tourCards.first());
    }
  }

  async getBookingCount(): Promise<number> {
    await this.waitForBookingsToLoad();
    return await this.tourCards.count();
  }

  async switchToTab(
    tabName:
      | "All tours"
      | "Booked"
      | "Confirmed"
      | "Started"
      | "Finished"
      | "Cancelled"
  ): Promise<void> {
    const tabLocator = this.page.locator(`button:has-text("${tabName}")`);
    await this.clickElement(tabLocator);
    await this.waitForNetworkIdle();
  }

  async getBookingDetails(bookingIndex: number = 0): Promise<{
    name: string;
    status: string;
    date: string;
    duration: string;
    guests: string;
  }> {
    const bookingCard = this.tourCards.nth(bookingIndex);

    return {
      name: await this.getElementText(
        bookingCard.locator('[data-testid="booking-name"]')
      ),
      status: await this.getElementText(
        bookingCard.locator('[data-testid="booking-status"]')
      ),
      date: await this.getElementText(
        bookingCard.locator('[data-testid="booking-date"]')
      ),
      duration: await this.getElementText(
        bookingCard.locator('[data-testid="booking-duration"]')
      ),
      guests: await this.getElementText(
        bookingCard.locator('[data-testid="booking-guests"]')
      ),
    };
  }

  async cancelBooking(bookingIndex: number = 0): Promise<void> {
    const bookingCard = this.tourCards.nth(bookingIndex);
    const cancelButton = bookingCard.locator('button:has-text("Cancel")');
    await this.clickElement(cancelButton);
  }

  async confirmCancellation(): Promise<void> {
    await this.verifyElementVisible(this.cancelModal);
    const confirmButton = this.cancelModal.locator(
      'button:has-text("Confirm")'
    );
    await this.clickElement(confirmButton);
    await this.waitForNetworkIdle();
  }

  async closeCancelModal(): Promise<void> {
    const closeButton = this.cancelModal.locator('[data-testid="close-modal"]');
    await this.clickElement(closeButton);
  }

  async uploadDocuments(bookingIndex: number = 0): Promise<void> {
    const bookingCard = this.tourCards.nth(bookingIndex);
    const uploadButton = bookingCard.locator(
      'button:has-text("Upload Documents")'
    );
    await this.clickElement(uploadButton);
  }

  async uploadFile(filePath: string): Promise<void> {
    await this.verifyElementVisible(this.uploadDocsModal);
    const fileInput = this.uploadDocsModal.locator('input[type="file"]');
    await fileInput.setInputFiles(filePath);
  }

  async submitDocumentUpload(): Promise<void> {
    const submitButton = this.uploadDocsModal.locator(
      'button:has-text("Upload")'
    );
    await this.clickElement(submitButton);
    await this.waitForNetworkIdle();
  }

  async closeUploadDocsModal(): Promise<void> {
    const closeButton = this.uploadDocsModal.locator(
      '[data-testid="close-modal"]'
    );
    await this.clickElement(closeButton);
  }

  async sendReview(bookingIndex: number = 0): Promise<void> {
    const bookingCard = this.tourCards.nth(bookingIndex);
    const reviewButton = bookingCard.locator('button:has-text("Send Review")');
    await this.clickElement(reviewButton);
  }

  async submitFeedback(rating: number, comment: string): Promise<void> {
    await this.verifyElementVisible(this.feedbackModal);

    // Select rating (assuming star rating system)
    const starRating = this.feedbackModal.locator(`[data-rating="${rating}"]`);
    await this.clickElement(starRating);

    // Fill comment
    const commentTextarea = this.feedbackModal.locator("textarea");
    await this.fillInput(commentTextarea, comment);

    // Submit
    const submitButton = this.feedbackModal.locator(
      'button:has-text("Submit")'
    );
    await this.clickElement(submitButton);
    await this.waitForNetworkIdle();
  }

  async closeFeedbackModal(): Promise<void> {
    const closeButton = this.feedbackModal.locator(
      '[data-testid="close-modal"]'
    );
    await this.clickElement(closeButton);
  }

  async editBooking(bookingIndex: number = 0): Promise<void> {
    const bookingCard = this.tourCards.nth(bookingIndex);
    const editButton = bookingCard.locator('button:has-text("Edit")');
    await this.clickElement(editButton);
  }

  async verifyBookingStatus(
    bookingIndex: number,
    expectedStatus: string
  ): Promise<void> {
    const bookingCard = this.tourCards.nth(bookingIndex);
    const statusElement = bookingCard.locator('[data-testid="booking-status"]');
    await this.verifyElementText(statusElement, expectedStatus);
  }

  async verifyBookingCardElements(bookingIndex: number = 0): Promise<void> {
    const bookingCard = this.tourCards.nth(bookingIndex);

    await this.verifyElementVisible(
      bookingCard.locator('[data-testid="booking-name"]')
    );
    await this.verifyElementVisible(
      bookingCard.locator('[data-testid="booking-status"]')
    );
    await this.verifyElementVisible(
      bookingCard.locator('[data-testid="booking-date"]')
    );
    await this.verifyElementVisible(
      bookingCard.locator('[data-testid="booking-duration"]')
    );
    await this.verifyElementVisible(
      bookingCard.locator('[data-testid="booking-guests"]')
    );
  }

  async verifyTabFiltering(
    tabName: string,
    expectedCount: number
  ): Promise<void> {
    await this.switchToTab(tabName as any);
    const actualCount = await this.getBookingCount();

    if (actualCount !== expectedCount) {
      throw new Error(
        `Expected ${expectedCount} bookings in ${tabName} tab, but found ${actualCount}`
      );
    }
  }

  async verifyNoBookingsMessage(): Promise<void> {
    await this.verifyElementVisible(this.noBookingsMessage);
  }

  async verifyLoadingState(): Promise<void> {
    await this.verifyElementVisible(this.loadingMessage);
  }

  async verifyErrorState(): Promise<void> {
    await this.verifyElementVisible(this.pageErrorMessage);
  }

  async verifyCancelModalContent(
    tourName: string,
    startDate: string
  ): Promise<void> {
    await this.verifyElementVisible(this.cancelModal);
    await this.verifyElementContainsText(this.cancelModal, tourName);
    await this.verifyElementContainsText(this.cancelModal, startDate);
  }

  async verifyUploadDocsModalContent(): Promise<void> {
    await this.verifyElementVisible(this.uploadDocsModal);
    await this.verifyElementVisible(
      this.uploadDocsModal.locator('input[type="file"]')
    );
  }

  async verifyFeedbackModalContent(): Promise<void> {
    await this.verifyElementVisible(this.feedbackModal);
    await this.verifyElementVisible(
      this.feedbackModal.locator('[data-testid="star-rating"]')
    );
    await this.verifyElementVisible(this.feedbackModal.locator("textarea"));
  }

  async getBookingsByStatus(status: string): Promise<number> {
    await this.switchToTab(status as any);
    return await this.getBookingCount();
  }

  async verifyBookingActionButtons(
    bookingIndex: number,
    expectedButtons: string[]
  ): Promise<void> {
    const bookingCard = this.tourCards.nth(bookingIndex);

    for (const buttonText of expectedButtons) {
      const button = bookingCard.locator(`button:has-text("${buttonText}")`);
      await this.verifyElementVisible(button);
    }
  }
}
