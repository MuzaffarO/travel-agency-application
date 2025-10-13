import { Locator, Page } from "@playwright/test";
import { BasePage } from "./BasePage";

export class LoginPage extends BasePage {
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly signInButton: Locator;
  readonly forgotPasswordLink: Locator;
  readonly createAccountLink: Locator;
  readonly welcomeText: Locator;
  readonly pageTitle: Locator;
  readonly emailError: Locator;
  readonly passwordError: Locator;

  constructor(page: Page) {
    super(page);
    this.emailInput = page.locator("#email");
    this.passwordInput = page.locator("#password");
    this.signInButton = page.locator('button:has-text("Sign in")');
    this.forgotPasswordLink = page.locator("text=Forgot password?");
    this.createAccountLink = page.locator('a[href="/register"]');
    this.welcomeText = page.locator("text=WELCOME BACK");
    this.pageTitle = page.locator('h1:has-text("Sign in to your account")');
    this.emailError = page.locator('[data-testid="email-error"]');
    this.passwordError = page.locator('[data-testid="password-error"]');
  }

  async navigateToLogin(): Promise<void> {
    await this.navigateTo("/login");
    await this.waitForPageLoad();
  }

  async login(email: string, password: string): Promise<void> {
    await this.fillInput(this.emailInput, email);
    await this.fillInput(this.passwordInput, password);
    await this.clickElement(this.signInButton);
  }

  async loginWithValidCredentials(
    email: string = "test@example.com",
    password: string = "password123"
  ): Promise<void> {
    await this.login(email, password);
    await this.waitForNetworkIdle();
  }

  async loginWithInvalidCredentials(
    email: string = "invalid@example.com",
    password: string = "wrongpassword"
  ): Promise<void> {
    await this.login(email, password);
    await this.waitForNetworkIdle();
  }

  async clickForgotPassword(): Promise<void> {
    await this.clickElement(this.forgotPasswordLink);
  }

  async clickCreateAccount(): Promise<void> {
    await this.clickElement(this.createAccountLink);
  }

  async verifyLoginPageElements(): Promise<void> {
    await this.verifyElementVisible(this.welcomeText);
    await this.verifyElementVisible(this.pageTitle);
    await this.verifyElementVisible(this.emailInput);
    await this.verifyElementVisible(this.passwordInput);
    await this.verifyElementVisible(this.signInButton);
    await this.verifyElementVisible(this.forgotPasswordLink);
    await this.verifyElementVisible(this.createAccountLink);
  }

  async verifySignInButtonDisabled(): Promise<void> {
    await this.page.waitForFunction(() => {
      const button = document.querySelector(
        'button:has-text("Sign in")'
      ) as HTMLButtonElement;
      return button?.disabled === true;
    });
  }

  async verifySignInButtonEnabled(): Promise<void> {
    await this.page.waitForFunction(() => {
      const button = document.querySelector(
        'button:has-text("Sign in")'
      ) as HTMLButtonElement;
      return button?.disabled === false;
    });
  }

  async verifyLoginError(expectedMessage: string): Promise<void> {
    const errorLocator = this.page.locator(`text=${expectedMessage}`);
    await this.verifyElementVisible(errorLocator);
  }

  async verifyEmailFieldError(): Promise<void> {
    await this.verifyElementVisible(this.emailError);
  }

  async verifyPasswordFieldError(): Promise<void> {
    await this.verifyElementVisible(this.passwordError);
  }

  async isSignInButtonEnabled(): Promise<boolean> {
    return await this.isElementEnabled(this.signInButton);
  }

  async clearLoginForm(): Promise<void> {
    await this.clearAndFill(this.emailInput, "");
    await this.clearAndFill(this.passwordInput, "");
  }

  async fillEmailOnly(email: string): Promise<void> {
    await this.fillInput(this.emailInput, email);
  }

  async fillPasswordOnly(password: string): Promise<void> {
    await this.fillInput(this.passwordInput, password);
  }

  async verifyRedirectAfterLogin(expectedUrl: string): Promise<void> {
    await this.page.waitForURL(expectedUrl);
    await this.verifyCurrentUrl(expectedUrl);
  }

  async verifyTravelAgentRedirect(): Promise<void> {
    await this.verifyRedirectAfterLogin("/");
  }

  async verifyCustomerRedirect(): Promise<void> {
    await this.verifyRedirectAfterLogin("/my-tours");
  }
}
