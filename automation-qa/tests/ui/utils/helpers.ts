import { BrowserContext, Page } from "@playwright/test";
import { breakpoints, testConfig } from "../fixtures/testData";

/**
 * Utility functions for UI tests
 */

// Date utilities
export class DateUtils {
  static getCurrentDate(): string {
    return new Date().toISOString().split("T")[0];
  }

  static getFutureDate(daysFromNow: number): string {
    const date = new Date();
    date.setDate(date.getDate() + daysFromNow);
    return date.toISOString().split("T")[0];
  }

  static getPastDate(daysAgo: number): string {
    const date = new Date();
    date.setDate(date.getDate() - daysAgo);
    return date.toISOString().split("T")[0];
  }

  static formatDateForDisplay(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    });
  }

  static isValidDate(dateString: string): boolean {
    const date = new Date(dateString);
    return date instanceof Date && !isNaN(date.getTime());
  }
}

// String utilities
export class StringUtils {
  static generateRandomEmail(): string {
    const timestamp = Date.now();
    const random = Math.random().toString(36).substring(2, 8);
    return `test${timestamp}${random}@example.com`;
  }

  static generateRandomString(length: number = 8): string {
    const chars =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    let result = "";
    for (let i = 0; i < length; i++) {
      result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return result;
  }

  static generateStrongPassword(): string {
    const uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    const lowercase = "abcdefghijklmnopqrstuvwxyz";
    const numbers = "0123456789";
    const symbols = "!@#$%^&*";

    let password = "";
    password += uppercase.charAt(Math.floor(Math.random() * uppercase.length));
    password += lowercase.charAt(Math.floor(Math.random() * lowercase.length));
    password += numbers.charAt(Math.floor(Math.random() * numbers.length));
    password += symbols.charAt(Math.floor(Math.random() * symbols.length));

    // Add random characters to reach minimum length
    const allChars = uppercase + lowercase + numbers + symbols;
    for (let i = password.length; i < 12; i++) {
      password += allChars.charAt(Math.floor(Math.random() * allChars.length));
    }

    // Shuffle the password
    return password
      .split("")
      .sort(() => Math.random() - 0.5)
      .join("");
  }

  static sanitizeFileName(fileName: string): string {
    return fileName.replace(/[^a-z0-9]/gi, "_").toLowerCase();
  }
}

// Wait utilities
export class WaitUtils {
  static async waitForTimeout(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }

  static async waitForCondition(
    condition: () => Promise<boolean> | boolean,
    timeout: number = testConfig.timeouts.medium,
    interval: number = 500
  ): Promise<void> {
    const startTime = Date.now();

    while (Date.now() - startTime < timeout) {
      if (await condition()) {
        return;
      }
      await this.waitForTimeout(interval);
    }

    throw new Error(`Condition not met within ${timeout}ms`);
  }

  static async waitForElementToDisappear(
    page: Page,
    selector: string,
    timeout: number = testConfig.timeouts.medium
  ): Promise<void> {
    await page.waitForSelector(selector, { state: "hidden", timeout });
  }

  static async waitForElementToAppear(
    page: Page,
    selector: string,
    timeout: number = testConfig.timeouts.medium
  ): Promise<void> {
    await page.waitForSelector(selector, { state: "visible", timeout });
  }
}

// Browser utilities
export class BrowserUtils {
  static async setViewportSize(
    page: Page,
    size: "mobile" | "tablet" | "desktop" | "largeDesktop"
  ): Promise<void> {
    const viewport = breakpoints[size];
    await page.setViewportSize(viewport);
  }

  static async simulateSlowNetwork(page: Page): Promise<void> {
    await page.route("**/*", (route) => {
      setTimeout(() => route.continue(), 1000);
    });
  }

  static async simulateOffline(page: Page): Promise<void> {
    await page.context().setOffline(true);
  }

  static async simulateOnline(page: Page): Promise<void> {
    await page.context().setOffline(false);
  }

  static async clearBrowserData(context: BrowserContext): Promise<void> {
    await context.clearCookies();
    await context.clearPermissions();
  }

  static async takeFullPageScreenshot(page: Page, name: string): Promise<void> {
    await page.screenshot({
      path: `screenshots/${name}-${Date.now()}.png`,
      fullPage: true,
    });
  }
}

// Form utilities
export class FormUtils {
  static async fillFormField(
    page: Page,
    selector: string,
    value: string,
    clearFirst: boolean = true
  ): Promise<void> {
    const field = page.locator(selector);
    if (clearFirst) {
      await field.clear();
    }
    await field.fill(value);
  }

  static async selectDropdownOption(
    page: Page,
    dropdownSelector: string,
    optionText: string
  ): Promise<void> {
    await page.locator(dropdownSelector).click();
    await page.locator(`text=${optionText}`).click();
  }

  static async uploadFile(
    page: Page,
    fileInputSelector: string,
    filePath: string
  ): Promise<void> {
    const fileInput = page.locator(fileInputSelector);
    await fileInput.setInputFiles(filePath);
  }

  static async submitForm(page: Page, formSelector: string): Promise<void> {
    await page.locator(formSelector).press("Enter");
  }

  static async validateFormField(
    page: Page,
    fieldSelector: string,
    expectedError: string
  ): Promise<void> {
    const errorElement = page.locator(
      `${fieldSelector} + .error-message, ${fieldSelector}-error`
    );
    await errorElement.waitFor({ state: "visible" });
    const errorText = await errorElement.textContent();
    if (!errorText?.includes(expectedError)) {
      throw new Error(
        `Expected error "${expectedError}" but got "${errorText}"`
      );
    }
  }
}

// API utilities
export class ApiUtils {
  static async mockApiCall(
    page: Page,
    url: string | RegExp,
    response: any,
    status: number = 200
  ): Promise<void> {
    await page.route(url, (route) => {
      route.fulfill({
        status,
        contentType: "application/json",
        body: JSON.stringify(response),
      });
    });
  }

  static async mockApiError(
    page: Page,
    url: string | RegExp,
    status: number = 500,
    message: string = "Server Error"
  ): Promise<void> {
    await page.route(url, (route) => {
      route.fulfill({
        status,
        contentType: "application/json",
        body: JSON.stringify({ error: message }),
      });
    });
  }

  static async interceptApiCall(
    page: Page,
    url: string | RegExp
  ): Promise<any> {
    return new Promise((resolve) => {
      page.route(url, async (route) => {
        const response = await route.fetch();
        const body = await response.json();
        resolve(body);
        route.continue();
      });
    });
  }

  static async waitForApiCall(
    page: Page,
    url: string | RegExp,
    timeout: number = testConfig.timeouts.medium
  ): Promise<void> {
    await page.waitForResponse(url, { timeout });
  }
}

// Storage utilities
export class StorageUtils {
  static async setLocalStorage(
    page: Page,
    key: string,
    value: any
  ): Promise<void> {
    await page.evaluate(
      ([key, value]) => {
        localStorage.setItem(key, JSON.stringify(value));
      },
      [key, value]
    );
  }

  static async getLocalStorage(page: Page, key: string): Promise<any> {
    return await page.evaluate((key) => {
      const value = localStorage.getItem(key);
      return value ? JSON.parse(value) : null;
    }, key);
  }

  static async clearLocalStorage(page: Page): Promise<void> {
    await page.evaluate(() => localStorage.clear());
  }

  static async setSessionStorage(
    page: Page,
    key: string,
    value: any
  ): Promise<void> {
    await page.evaluate(
      ([key, value]) => {
        sessionStorage.setItem(key, JSON.stringify(value));
      },
      [key, value]
    );
  }

  static async clearSessionStorage(page: Page): Promise<void> {
    await page.evaluate(() => sessionStorage.clear());
  }
}

// Authentication utilities
export class AuthUtils {
  static async loginUser(
    page: Page,
    email: string,
    password: string
  ): Promise<void> {
    await page.goto("/login");
    await page.fill("#email", email);
    await page.fill("#password", password);
    await page.click('button:has-text("Sign in")');
    await page.waitForLoadState("networkidle");
  }

  static async logoutUser(page: Page): Promise<void> {
    // Assuming there's a logout button in the header
    await page.click('[data-testid="logout-button"]');
    await page.waitForURL("/login");
  }

  static async isUserLoggedIn(page: Page): Promise<boolean> {
    try {
      await page.waitForSelector('[data-testid="user-menu"]', {
        timeout: 5000,
      });
      return true;
    } catch {
      return false;
    }
  }

  static async setAuthToken(page: Page, token: string): Promise<void> {
    await this.setLocalStorage(page, "authToken", token);
  }

  static async getAuthToken(page: Page): Promise<string | null> {
    return await this.getLocalStorage(page, "authToken");
  }
}

// Accessibility utilities
export class AccessibilityUtils {
  static async checkKeyboardNavigation(
    page: Page,
    selectors: string[]
  ): Promise<void> {
    for (const selector of selectors) {
      await page.focus(selector);
      const focusedElement = await page.evaluate(
        () => document.activeElement?.tagName
      );
      if (!focusedElement) {
        throw new Error(`Element ${selector} is not focusable`);
      }
    }
  }

  static async checkAriaLabels(page: Page, selectors: string[]): Promise<void> {
    for (const selector of selectors) {
      const element = page.locator(selector);
      const ariaLabel = await element.getAttribute("aria-label");
      const ariaLabelledBy = await element.getAttribute("aria-labelledby");

      if (!ariaLabel && !ariaLabelledBy) {
        throw new Error(
          `Element ${selector} missing aria-label or aria-labelledby`
        );
      }
    }
  }

  static async checkColorContrast(page: Page): Promise<void> {
    // This would require integration with accessibility testing tools
    // For now, we'll just check that text is visible
    const textElements = await page
      .locator("p, h1, h2, h3, h4, h5, h6, span, a")
      .all();

    for (const element of textElements) {
      const isVisible = await element.isVisible();
      if (!isVisible) {
        const text = await element.textContent();
        console.warn(`Text element with content "${text}" is not visible`);
      }
    }
  }
}

// Performance utilities
export class PerformanceUtils {
  static async measurePageLoadTime(page: Page, url: string): Promise<number> {
    const startTime = Date.now();
    await page.goto(url);
    await page.waitForLoadState("networkidle");
    return Date.now() - startTime;
  }

  static async measureElementLoadTime(
    page: Page,
    selector: string
  ): Promise<number> {
    const startTime = Date.now();
    await page.waitForSelector(selector, { state: "visible" });
    return Date.now() - startTime;
  }

  static async getPageMetrics(page: Page): Promise<any> {
    return await page.evaluate(() => {
      const navigation = performance.getEntriesByType(
        "navigation"
      )[0] as PerformanceNavigationTiming;
      return {
        domContentLoaded:
          navigation.domContentLoadedEventEnd -
          navigation.domContentLoadedEventStart,
        loadComplete: navigation.loadEventEnd - navigation.loadEventStart,
        firstPaint:
          performance.getEntriesByName("first-paint")[0]?.startTime || 0,
        firstContentfulPaint:
          performance.getEntriesByName("first-contentful-paint")[0]
            ?.startTime || 0,
      };
    });
  }
}

// Test data generators
export class TestDataGenerator {
  static generateUser(overrides: Partial<any> = {}): any {
    return {
      firstName: "Test",
      lastName: "User",
      email: StringUtils.generateRandomEmail(),
      password: StringUtils.generateStrongPassword(),
      ...overrides,
    };
  }

  static generateBooking(overrides: Partial<any> = {}): any {
    return {
      tourId: "tour-1",
      startDate: DateUtils.getFutureDate(30),
      endDate: DateUtils.getFutureDate(37),
      adults: 2,
      children: 0,
      mealPlan: "Half Board",
      ...overrides,
    };
  }

  static generateSearchFilters(overrides: Partial<any> = {}): any {
    return {
      location: "Paris",
      startDate: DateUtils.getFutureDate(30),
      endDate: DateUtils.getFutureDate(37),
      adults: 2,
      children: 0,
      ...overrides,
    };
  }
}

// Retry utilities
export class RetryUtils {
  static async retry<T>(
    fn: () => Promise<T>,
    maxAttempts: number = 3,
    delay: number = 1000
  ): Promise<T> {
    let lastError: Error;

    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        return await fn();
      } catch (error) {
        lastError = error as Error;
        if (attempt === maxAttempts) {
          throw lastError;
        }
        await WaitUtils.waitForTimeout(delay * attempt);
      }
    }

    throw lastError!;
  }

  static async retryUntilSuccess<T>(
    fn: () => Promise<T>,
    timeout: number = testConfig.timeouts.long,
    interval: number = 1000
  ): Promise<T> {
    const startTime = Date.now();
    let lastError: Error;

    while (Date.now() - startTime < timeout) {
      try {
        return await fn();
      } catch (error) {
        lastError = error as Error;
        await WaitUtils.waitForTimeout(interval);
      }
    }

    throw lastError!;
  }
}
