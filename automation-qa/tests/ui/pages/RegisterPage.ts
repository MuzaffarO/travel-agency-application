import { Locator, Page } from "@playwright/test";
import { BasePage } from "./BasePage";

export class RegisterPage extends BasePage {
  readonly firstNameInput: Locator;
  readonly lastNameInput: Locator;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly confirmPasswordInput: Locator;
  readonly createAccountButton: Locator;
  readonly loginLink: Locator;
  readonly pageTitle: Locator;
  readonly welcomeText: Locator;
  readonly passwordRules: Locator;
  readonly firstNameError: Locator;
  readonly lastNameError: Locator;
  readonly emailError: Locator;
  readonly passwordError: Locator;
  readonly confirmPasswordError: Locator;

  constructor(page: Page) {
    super(page);
    this.firstNameInput = page.locator("#firstName");
    this.lastNameInput = page.locator("#lastName");
    this.emailInput = page.locator("#email");
    this.passwordInput = page.locator("#password");
    this.confirmPasswordInput = page.locator("#confirmPassword");
    this.createAccountButton = page.locator(
      'button:has-text("Create an account")'
    );
    this.loginLink = page.locator('a[href="/login"]');
    this.pageTitle = page.locator('h1:has-text("Create an account")');
    this.welcomeText = page.locator("text=LET'S GET YOU STARTED");
    this.passwordRules = page.locator('[data-testid="password-rules"]');
    this.firstNameError = page.locator('[data-testid="firstName-error"]');
    this.lastNameError = page.locator('[data-testid="lastName-error"]');
    this.emailError = page.locator('[data-testid="email-error"]');
    this.passwordError = page.locator('[data-testid="password-error"]');
    this.confirmPasswordError = page.locator(
      '[data-testid="confirmPassword-error"]'
    );
  }

  async navigateToRegister(): Promise<void> {
    await this.navigateTo("/register");
    await this.waitForPageLoad();
  }

  async fillRegistrationForm(userData: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    confirmPassword: string;
  }): Promise<void> {
    await this.fillInput(this.firstNameInput, userData.firstName);
    await this.fillInput(this.lastNameInput, userData.lastName);
    await this.fillInput(this.emailInput, userData.email);
    await this.fillInput(this.passwordInput, userData.password);
    await this.fillInput(this.confirmPasswordInput, userData.confirmPassword);
  }

  async submitRegistration(): Promise<void> {
    await this.clickElement(this.createAccountButton);
    await this.waitForNetworkIdle();
  }

  async registerUser(userData: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    confirmPassword: string;
  }): Promise<void> {
    await this.fillRegistrationForm(userData);
    await this.submitRegistration();
  }

  async registerWithValidData(): Promise<void> {
    const userData = {
      firstName: "John",
      lastName: "Doe",
      email: `test${Date.now()}@example.com`,
      password: "SecurePass123!",
      confirmPassword: "SecurePass123!",
    };
    await this.registerUser(userData);
  }

  async registerWithExistingEmail(
    email: string = "existing@example.com"
  ): Promise<void> {
    const userData = {
      firstName: "John",
      lastName: "Doe",
      email: email,
      password: "SecurePass123!",
      confirmPassword: "SecurePass123!",
    };
    await this.registerUser(userData);
  }

  async registerWithWeakPassword(): Promise<void> {
    const userData = {
      firstName: "John",
      lastName: "Doe",
      email: `test${Date.now()}@example.com`,
      password: "123",
      confirmPassword: "123",
    };
    await this.registerUser(userData);
  }

  async registerWithMismatchedPasswords(): Promise<void> {
    const userData = {
      firstName: "John",
      lastName: "Doe",
      email: `test${Date.now()}@example.com`,
      password: "SecurePass123!",
      confirmPassword: "DifferentPass123!",
    };
    await this.registerUser(userData);
  }

  async clickLoginLink(): Promise<void> {
    await this.clickElement(this.loginLink);
  }

  async verifyRegistrationPageElements(): Promise<void> {
    await this.verifyElementVisible(this.welcomeText);
    await this.verifyElementVisible(this.pageTitle);
    await this.verifyElementVisible(this.firstNameInput);
    await this.verifyElementVisible(this.lastNameInput);
    await this.verifyElementVisible(this.emailInput);
    await this.verifyElementVisible(this.passwordInput);
    await this.verifyElementVisible(this.confirmPasswordInput);
    await this.verifyElementVisible(this.createAccountButton);
    await this.verifyElementVisible(this.loginLink);
  }

  async verifyCreateAccountButtonDisabled(): Promise<void> {
    await this.page.waitForFunction(() => {
      const button = document.querySelector(
        'button:has-text("Create an account")'
      ) as HTMLButtonElement;
      return button?.disabled === true;
    });
  }

  async verifyCreateAccountButtonEnabled(): Promise<void> {
    await this.page.waitForFunction(() => {
      const button = document.querySelector(
        'button:has-text("Create an account")'
      ) as HTMLButtonElement;
      return button?.disabled === false;
    });
  }

  async verifySuccessToast(): Promise<void> {
    const successToast = this.page.locator(
      "text=Your account has been created successfully"
    );
    await this.verifyElementVisible(successToast);
  }

  async verifyEmailExistsError(): Promise<void> {
    const errorMessage = this.page.locator("text=Email already exists");
    await this.verifyElementVisible(errorMessage);
  }

  async verifyPasswordValidationRules(): Promise<void> {
    // Check if password rules are displayed
    await this.verifyElementVisible(this.passwordRules);
  }

  async verifyFormValidationErrors(): Promise<void> {
    // This method can be expanded to check specific validation errors
    const hasErrors =
      (await this.page.locator('[data-testid*="error"]').count()) > 0;
    if (!hasErrors) {
      throw new Error("Expected form validation errors but none were found");
    }
  }

  async verifyRedirectToLogin(): Promise<void> {
    await this.page.waitForURL("/login");
    await this.verifyCurrentUrl("/login");
  }

  async clearRegistrationForm(): Promise<void> {
    await this.clearAndFill(this.firstNameInput, "");
    await this.clearAndFill(this.lastNameInput, "");
    await this.clearAndFill(this.emailInput, "");
    await this.clearAndFill(this.passwordInput, "");
    await this.clearAndFill(this.confirmPasswordInput, "");
  }

  async fillPartialForm(
    fields: Partial<{
      firstName: string;
      lastName: string;
      email: string;
      password: string;
      confirmPassword: string;
    }>
  ): Promise<void> {
    if (fields.firstName)
      await this.fillInput(this.firstNameInput, fields.firstName);
    if (fields.lastName)
      await this.fillInput(this.lastNameInput, fields.lastName);
    if (fields.email) await this.fillInput(this.emailInput, fields.email);
    if (fields.password)
      await this.fillInput(this.passwordInput, fields.password);
    if (fields.confirmPassword)
      await this.fillInput(this.confirmPasswordInput, fields.confirmPassword);
  }

  async isCreateAccountButtonEnabled(): Promise<boolean> {
    return await this.isElementEnabled(this.createAccountButton);
  }

  async getPasswordStrengthIndicator(): Promise<string> {
    const indicator = this.page.locator('[data-testid="password-strength"]');
    return await this.getElementText(indicator);
  }
}
