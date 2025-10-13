import { Locator, Page, expect } from "@playwright/test";

export class BasePage {
  readonly page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  // Common elements
  get header(): Locator {
    return this.page.locator("header");
  }

  get footer(): Locator {
    return this.page.locator("footer");
  }

  get loadingSpinner(): Locator {
    return this.page.locator('[data-testid="loading"]');
  }

  get errorMessage(): Locator {
    return this.page.locator('[data-testid="error-message"]');
  }

  get successToast(): Locator {
    return this.page.locator('[data-testid="success-toast"]');
  }

  // Common actions
  async navigateTo(url: string): Promise<void> {
    await this.page.goto(url);
  }

  async waitForPageLoad(): Promise<void> {
    await this.page.waitForLoadState("networkidle");
  }

  async waitForElement(
    locator: Locator,
    timeout: number = 10000
  ): Promise<void> {
    await locator.waitFor({ state: "visible", timeout });
  }

  async clickElement(locator: Locator): Promise<void> {
    await locator.click();
  }

  async fillInput(locator: Locator, text: string): Promise<void> {
    await locator.fill(text);
  }

  async selectDropdownOption(dropdown: Locator, option: string): Promise<void> {
    await dropdown.click();
    await this.page.locator(`text=${option}`).click();
  }

  async takeScreenshot(name: string): Promise<void> {
    await this.page.screenshot({ path: `screenshots/${name}.png` });
  }

  async verifyElementVisible(locator: Locator): Promise<void> {
    await expect(locator).toBeVisible();
  }

  async verifyElementHidden(locator: Locator): Promise<void> {
    await expect(locator).toBeHidden();
  }

  async verifyElementText(
    locator: Locator,
    expectedText: string
  ): Promise<void> {
    await expect(locator).toHaveText(expectedText);
  }

  async verifyElementContainsText(
    locator: Locator,
    expectedText: string
  ): Promise<void> {
    await expect(locator).toContainText(expectedText);
  }

  async verifyPageTitle(expectedTitle: string): Promise<void> {
    await expect(this.page).toHaveTitle(expectedTitle);
  }

  async verifyCurrentUrl(expectedUrl: string): Promise<void> {
    expect(this.page.url()).toContain(expectedUrl);
  }

  // Utility methods
  async scrollToElement(locator: Locator): Promise<void> {
    await locator.scrollIntoViewIfNeeded();
  }

  async waitForNetworkIdle(): Promise<void> {
    await this.page.waitForLoadState("networkidle");
  }

  async clearAndFill(locator: Locator, text: string): Promise<void> {
    await locator.clear();
    await locator.fill(text);
  }

  async pressKey(key: string): Promise<void> {
    await this.page.keyboard.press(key);
  }

  async getElementText(locator: Locator): Promise<string> {
    return (await locator.textContent()) || "";
  }

  async getElementAttribute(
    locator: Locator,
    attribute: string
  ): Promise<string> {
    return (await locator.getAttribute(attribute)) || "";
  }

  async isElementVisible(locator: Locator): Promise<boolean> {
    return await locator.isVisible();
  }

  async isElementEnabled(locator: Locator): Promise<boolean> {
    return await locator.isEnabled();
  }

  async waitForResponse(urlPattern: string | RegExp): Promise<void> {
    await this.page.waitForResponse(urlPattern);
  }

  async mockApiResponse(url: string | RegExp, response: any): Promise<void> {
    await this.page.route(url, (route) => {
      route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify(response),
      });
    });
  }
}
