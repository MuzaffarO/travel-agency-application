import { Locator, Page } from "@playwright/test";
import { BasePage } from "./BasePage";

export class MainPage extends BasePage {
  readonly pageTitle: Locator;
  readonly searchSection: Locator;
  readonly locationDropdown: Locator;
  readonly dateSelector: Locator;
  readonly guestsSelector: Locator;
  readonly mealSelector: Locator;
  readonly searchButton: Locator;
  readonly sortDropdown: Locator;
  readonly tourCards: Locator;
  readonly loadingMessage: Locator;
  readonly errorMessage: Locator;
  readonly noResultsMessage: Locator;
  readonly bookingModal: Locator;
  readonly notLoggedModal: Locator;

  constructor(page: Page) {
    super(page);
    this.pageTitle = page.locator('h1:has-text("Search for your next tour")');
    this.searchSection = page.locator('[data-testid="search-section"]');
    this.locationDropdown = page.locator('[data-testid="location-dropdown"]');
    this.dateSelector = page.locator('[data-testid="date-selector"]');
    this.guestsSelector = page.locator('[data-testid="guests-selector"]');
    this.mealSelector = page.locator('[data-testid="meal-selector"]');
    this.searchButton = page.locator('button:has-text("Search")');
    this.sortDropdown = page.locator('[data-testid="sort-dropdown"]');
    this.tourCards = page.locator('[data-testid="tour-card"]');
    this.loadingMessage = page.locator("text=Loading tours...");
    this.errorMessage = page.locator('[class*="text-red"]');
    this.noResultsMessage = page.locator("text=No tours found");
    this.bookingModal = page.locator('[data-testid="booking-modal"]');
    this.notLoggedModal = page.locator('[data-testid="not-logged-modal"]');
  }

  async navigateToMain(): Promise<void> {
    await this.navigateTo("/");
    await this.waitForPageLoad();
  }

  async waitForToursToLoad(): Promise<void> {
    // Wait for loading to disappear and tours to appear
    await this.page.waitForFunction(
      () => {
        const loading = document.querySelector("text=Loading tours...");
        const tours = document.querySelectorAll('[data-testid="tour-card"]');
        return !loading && tours.length > 0;
      },
      { timeout: 15000 }
    );
  }

  async verifyMainPageElements(): Promise<void> {
    await this.verifyElementVisible(this.pageTitle);
    await this.verifyElementVisible(this.searchSection);
    await this.verifyElementVisible(this.sortDropdown);
  }

  async verifyToursDisplayed(): Promise<void> {
    await this.waitForToursToLoad();
    await this.verifyElementVisible(this.tourCards.first());
  }

  async getTourCount(): Promise<number> {
    await this.waitForToursToLoad();
    return await this.tourCards.count();
  }

  async selectLocation(location: string): Promise<void> {
    await this.clickElement(this.locationDropdown);
    await this.page.locator(`text=${location}`).click();
  }

  async selectDates(startDate: string, endDate: string): Promise<void> {
    await this.clickElement(this.dateSelector);
    // Implement date selection logic based on your calendar component
    await this.page.locator(`[data-date="${startDate}"]`).click();
    await this.page.locator(`[data-date="${endDate}"]`).click();
  }

  async selectGuests(adults: number, children: number = 0): Promise<void> {
    await this.clickElement(this.guestsSelector);

    // Set adults count
    const adultsInput = this.page.locator('[data-testid="adults-input"]');
    await this.clearAndFill(adultsInput, adults.toString());

    // Set children count if specified
    if (children > 0) {
      const childrenInput = this.page.locator('[data-testid="children-input"]');
      await this.clearAndFill(childrenInput, children.toString());
    }

    // Close dropdown
    await this.pressKey("Escape");
  }

  async selectMealPlan(mealPlan: string): Promise<void> {
    await this.clickElement(this.mealSelector);
    await this.page.locator(`text=${mealPlan}`).click();
  }

  async performSearch(): Promise<void> {
    await this.clickElement(this.searchButton);
    await this.waitForNetworkIdle();
  }

  async searchTours(filters: {
    location?: string;
    startDate?: string;
    endDate?: string;
    adults?: number;
    children?: number;
    mealPlan?: string;
  }): Promise<void> {
    if (filters.location) await this.selectLocation(filters.location);
    if (filters.startDate && filters.endDate) {
      await this.selectDates(filters.startDate, filters.endDate);
    }
    if (filters.adults)
      await this.selectGuests(filters.adults, filters.children);
    if (filters.mealPlan) await this.selectMealPlan(filters.mealPlan);

    await this.performSearch();
  }

  async sortTours(sortOption: string): Promise<void> {
    await this.selectDropdownOption(this.sortDropdown, sortOption);
    await this.waitForNetworkIdle();
  }

  async clickBookTour(tourIndex: number = 0): Promise<void> {
    const bookButton = this.tourCards
      .nth(tourIndex)
      .locator('button:has-text("Book")');
    await this.clickElement(bookButton);
  }

  async getTourDetails(tourIndex: number = 0): Promise<{
    name: string;
    location: string;
    price: string;
    rating: string;
  }> {
    const tourCard = this.tourCards.nth(tourIndex);

    return {
      name: await this.getElementText(
        tourCard.locator('[data-testid="tour-name"]')
      ),
      location: await this.getElementText(
        tourCard.locator('[data-testid="tour-location"]')
      ),
      price: await this.getElementText(
        tourCard.locator('[data-testid="tour-price"]')
      ),
      rating: await this.getElementText(
        tourCard.locator('[data-testid="tour-rating"]')
      ),
    };
  }

  async verifyBookingModalOpened(): Promise<void> {
    await this.verifyElementVisible(this.bookingModal);
  }

  async verifyNotLoggedModalOpened(): Promise<void> {
    await this.verifyElementVisible(this.notLoggedModal);
  }

  async closeBookingModal(): Promise<void> {
    const closeButton = this.bookingModal.locator(
      '[data-testid="close-modal"]'
    );
    await this.clickElement(closeButton);
  }

  async closeNotLoggedModal(): Promise<void> {
    const closeButton = this.notLoggedModal.locator(
      '[data-testid="close-modal"]'
    );
    await this.clickElement(closeButton);
  }

  async verifyLoadingState(): Promise<void> {
    await this.verifyElementVisible(this.loadingMessage);
  }

  async verifyErrorState(): Promise<void> {
    await this.verifyElementVisible(this.errorMessage);
  }

  async verifyNoResultsState(): Promise<void> {
    await this.verifyElementVisible(this.noResultsMessage);
  }

  async verifyTourCardElements(tourIndex: number = 0): Promise<void> {
    const tourCard = this.tourCards.nth(tourIndex);

    await this.verifyElementVisible(
      tourCard.locator('[data-testid="tour-name"]')
    );
    await this.verifyElementVisible(
      tourCard.locator('[data-testid="tour-location"]')
    );
    await this.verifyElementVisible(
      tourCard.locator('[data-testid="tour-price"]')
    );
    await this.verifyElementVisible(
      tourCard.locator('[data-testid="tour-rating"]')
    );
    await this.verifyElementVisible(
      tourCard.locator('[data-testid="tour-image"]')
    );
    await this.verifyElementVisible(
      tourCard.locator('button:has-text("Book")')
    );
  }

  async verifyResponsiveLayout(
    screenSize: "desktop" | "tablet" | "mobile"
  ): Promise<void> {
    const expectedColumns = {
      desktop: 2,
      tablet: 2,
      mobile: 1,
    };

    // This is a simplified check - you might need to adjust based on your CSS classes
    const gridContainer = this.page.locator(".grid");
    const computedStyle = await gridContainer.evaluate(
      (el) => window.getComputedStyle(el).gridTemplateColumns
    );

    // Verify the grid has the expected number of columns
    const columnCount = computedStyle.split(" ").length;
    if (columnCount !== expectedColumns[screenSize]) {
      throw new Error(
        `Expected ${expectedColumns[screenSize]} columns but found ${columnCount}`
      );
    }
  }

  async searchWithEmptyFilters(): Promise<void> {
    await this.performSearch();
  }

  async clearAllFilters(): Promise<void> {
    // Implementation depends on your filter reset functionality
    const clearButton = this.page.locator('[data-testid="clear-filters"]');
    if (await clearButton.isVisible()) {
      await this.clickElement(clearButton);
    }
  }

  async verifyFiltersPersisted(): Promise<void> {
    // Verify that selected filters remain after page operations
    // Implementation depends on your specific filter UI
  }
}
