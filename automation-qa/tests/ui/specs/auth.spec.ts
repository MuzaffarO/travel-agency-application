import { expect, test } from "@playwright/test";
import { errorMessages, testUsers } from "../fixtures/testData";
import { LoginPage } from "../pages/LoginPage";
import { MainPage } from "../pages/MainPage";
import { RegisterPage } from "../pages/RegisterPage";
import { TIMEOUTS, URLS } from "../utils/constants";
import { StringUtils, TestDataGenerator } from "../utils/helpers";

test.describe("Authentication Tests", () => {
  let loginPage: LoginPage;
  let registerPage: RegisterPage;
  let mainPage: MainPage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    registerPage = new RegisterPage(page);
    mainPage = new MainPage(page);
  });

  test.describe("User Registration", () => {
    test("TC-UI-001: User Registration - Happy Path", async ({ page }) => {
      const userData = TestDataGenerator.generateUser();

      await registerPage.navigateToRegister();
      await registerPage.verifyRegistrationPageElements();

      await registerPage.registerUser({
        ...userData,
        confirmPassword: userData.password,
      });

      await registerPage.verifySuccessToast();
      await registerPage.verifyRedirectToLogin();
    });

    test("TC-UI-002: User Registration - Email Already Exists", async ({
      page,
    }) => {
      await registerPage.navigateToRegister();

      await registerPage.registerWithExistingEmail(
        testUsers.existingUser.email
      );

      await registerPage.verifyEmailExistsError();
    });

    test("TC-UI-003: User Registration - Password Validation", async ({
      page,
    }) => {
      await registerPage.navigateToRegister();

      // Test weak password
      await registerPage.fillPartialForm({
        firstName: "Test",
        lastName: "User",
        email: StringUtils.generateRandomEmail(),
        password: "123",
      });

      await registerPage.verifyPasswordValidationRules();
      await registerPage.verifyCreateAccountButtonDisabled();

      // Test strong password
      await registerPage.clearRegistrationForm();
      const strongPassword = StringUtils.generateStrongPassword();
      await registerPage.fillPartialForm({
        firstName: "Test",
        lastName: "User",
        email: StringUtils.generateRandomEmail(),
        password: strongPassword,
        confirmPassword: strongPassword,
      });

      await registerPage.verifyCreateAccountButtonEnabled();
    });

    test("TC-UI-004: User Registration - Password Mismatch", async ({
      page,
    }) => {
      await registerPage.navigateToRegister();

      await registerPage.registerWithMismatchedPasswords();

      await registerPage.verifyFormValidationErrors();
    });

    test("TC-UI-005: User Registration - Required Fields Validation", async ({
      page,
    }) => {
      await registerPage.navigateToRegister();

      // Try to submit empty form
      await registerPage.submitRegistration();

      await registerPage.verifyFormValidationErrors();
      await registerPage.verifyCreateAccountButtonDisabled();
    });

    test("TC-UI-006: User Registration - Navigation to Login", async ({
      page,
    }) => {
      await registerPage.navigateToRegister();

      await registerPage.clickLoginLink();

      await expect(page).toHaveURL(URLS.LOGIN);
      await loginPage.verifyLoginPageElements();
    });
  });

  test.describe("User Login", () => {
    test("TC-UI-007: User Login - Valid Credentials (Customer)", async ({
      page,
    }) => {
      await loginPage.navigateToLogin();
      await loginPage.verifyLoginPageElements();

      await loginPage.loginWithValidCredentials(
        testUsers.validCustomer.email,
        testUsers.validCustomer.password
      );

      await loginPage.verifyCustomerRedirect();
    });

    test("TC-UI-008: User Login - Valid Credentials (Travel Agent)", async ({
      page,
    }) => {
      await loginPage.navigateToLogin();

      await loginPage.loginWithValidCredentials(
        testUsers.validTravelAgent.email,
        testUsers.validTravelAgent.password
      );

      await loginPage.verifyTravelAgentRedirect();
    });

    test("TC-UI-009: User Login - Invalid Credentials", async ({ page }) => {
      await loginPage.navigateToLogin();

      await loginPage.loginWithInvalidCredentials();

      await loginPage.verifyLoginError(errorMessages.auth.invalidCredentials);
    });

    test("TC-UI-010: User Login - Empty Fields", async ({ page }) => {
      await loginPage.navigateToLogin();

      await loginPage.clickElement(loginPage.signInButton);

      await loginPage.verifySignInButtonDisabled();
    });

    test("TC-UI-011: User Login - Form Validation", async ({ page }) => {
      await loginPage.navigateToLogin();

      // Test email field only
      await loginPage.fillEmailOnly("test@example.com");
      await loginPage.verifySignInButtonDisabled();

      // Test password field only
      await loginPage.clearLoginForm();
      await loginPage.fillPasswordOnly("password123");
      await loginPage.verifySignInButtonDisabled();

      // Test both fields filled
      await loginPage.fillEmailOnly("test@example.com");
      await loginPage.verifySignInButtonEnabled();
    });

    test("TC-UI-012: User Login - Navigation to Register", async ({ page }) => {
      await loginPage.navigateToLogin();

      await loginPage.clickCreateAccount();

      await expect(page).toHaveURL(URLS.REGISTER);
      await registerPage.verifyRegistrationPageElements();
    });

    test("TC-UI-013: User Login - Forgot Password", async ({ page }) => {
      await loginPage.navigateToLogin();

      await loginPage.clickForgotPassword();

      // Verify forgot password flow is initiated
      // This depends on your implementation
    });
  });

  test.describe("Authentication State Management", () => {
    test("TC-UI-014: Persistent Login Session", async ({ page, context }) => {
      // Login user
      await loginPage.navigateToLogin();
      await loginPage.loginWithValidCredentials(
        testUsers.validCustomer.email,
        testUsers.validCustomer.password
      );

      // Navigate to different page
      await mainPage.navigateToMain();

      // Verify user is still logged in
      const userMenu = page.locator('[data-testid="user-menu"]');
      await expect(userMenu).toBeVisible();
    });

    test("TC-UI-015: Logout Functionality", async ({ page }) => {
      // Login user first
      await loginPage.navigateToLogin();
      await loginPage.loginWithValidCredentials(
        testUsers.validCustomer.email,
        testUsers.validCustomer.password
      );

      // Logout
      const logoutButton = page.locator('[data-testid="logout-button"]');
      await logoutButton.click();

      // Verify redirect to login
      await expect(page).toHaveURL(URLS.LOGIN);
    });

    test("TC-UI-016: Protected Route Access", async ({ page }) => {
      // Try to access protected route without login
      await page.goto(URLS.MY_TOURS);

      // Should redirect to login
      await expect(page).toHaveURL(URLS.LOGIN);
    });
  });

  test.describe("Form Accessibility", () => {
    test("TC-UI-017: Login Form Keyboard Navigation", async ({ page }) => {
      await loginPage.navigateToLogin();

      // Test tab navigation
      await page.keyboard.press("Tab");
      await expect(loginPage.emailInput).toBeFocused();

      await page.keyboard.press("Tab");
      await expect(loginPage.passwordInput).toBeFocused();

      await page.keyboard.press("Tab");
      await expect(loginPage.signInButton).toBeFocused();
    });

    test("TC-UI-018: Register Form Keyboard Navigation", async ({ page }) => {
      await registerPage.navigateToRegister();

      // Test tab navigation through all fields
      await page.keyboard.press("Tab");
      await expect(registerPage.firstNameInput).toBeFocused();

      await page.keyboard.press("Tab");
      await expect(registerPage.lastNameInput).toBeFocused();

      await page.keyboard.press("Tab");
      await expect(registerPage.emailInput).toBeFocused();

      await page.keyboard.press("Tab");
      await expect(registerPage.passwordInput).toBeFocused();

      await page.keyboard.press("Tab");
      await expect(registerPage.confirmPasswordInput).toBeFocused();
    });

    test("TC-UI-019: Form Labels and ARIA Attributes", async ({ page }) => {
      await loginPage.navigateToLogin();

      // Check email input has proper label
      const emailLabel = await loginPage.emailInput.getAttribute("aria-label");
      expect(emailLabel).toBeTruthy();

      // Check password input has proper label
      const passwordLabel = await loginPage.passwordInput.getAttribute(
        "aria-label"
      );
      expect(passwordLabel).toBeTruthy();
    });
  });

  test.describe("Responsive Design", () => {
    test("TC-UI-020: Login Page Mobile View", async ({ page }) => {
      await page.setViewportSize({ width: 375, height: 667 });
      await loginPage.navigateToLogin();

      await loginPage.verifyLoginPageElements();

      // Verify form is still functional on mobile
      await loginPage.fillInput(loginPage.emailInput, "test@example.com");
      await loginPage.fillInput(loginPage.passwordInput, "password123");
      await expect(loginPage.signInButton).toBeEnabled();
    });

    test("TC-UI-021: Register Page Tablet View", async ({ page }) => {
      await page.setViewportSize({ width: 768, height: 1024 });
      await registerPage.navigateToRegister();

      await registerPage.verifyRegistrationPageElements();

      // Verify form layout adapts properly
      const firstNameInput = registerPage.firstNameInput;
      const lastNameInput = registerPage.lastNameInput;

      await expect(firstNameInput).toBeVisible();
      await expect(lastNameInput).toBeVisible();
    });
  });

  test.describe("Error Handling", () => {
    test("TC-UI-022: Network Error During Login", async ({ page }) => {
      await loginPage.navigateToLogin();

      // Mock network error
      await page.route("**/api/auth/login", (route) => {
        route.abort("failed");
      });

      await loginPage.loginWithValidCredentials();

      // Verify error handling
      const errorMessage = page.locator("text=Network error");
      await expect(errorMessage).toBeVisible({ timeout: TIMEOUTS.MEDIUM });
    });

    test("TC-UI-023: Server Error During Registration", async ({ page }) => {
      await registerPage.navigateToRegister();

      // Mock server error
      await page.route("**/api/auth/register", (route) => {
        route.fulfill({
          status: 500,
          contentType: "application/json",
          body: JSON.stringify({ error: "Internal server error" }),
        });
      });

      const userData = TestDataGenerator.generateUser();
      await registerPage.registerUser({
        ...userData,
        confirmPassword: userData.password,
      });

      // Verify error handling
      const errorMessage = page.locator("text=Server error");
      await expect(errorMessage).toBeVisible({ timeout: TIMEOUTS.MEDIUM });
    });
  });

  test.describe("Performance", () => {
    test("TC-UI-024: Login Page Load Performance", async ({ page }) => {
      const startTime = Date.now();
      await loginPage.navigateToLogin();
      const loadTime = Date.now() - startTime;

      expect(loadTime).toBeLessThan(3000); // Should load within 3 seconds
    });

    test("TC-UI-025: Registration Form Submission Performance", async ({
      page,
    }) => {
      await registerPage.navigateToRegister();

      const userData = TestDataGenerator.generateUser();
      await registerPage.fillRegistrationForm({
        ...userData,
        confirmPassword: userData.password,
      });

      const startTime = Date.now();
      await registerPage.submitRegistration();
      const submitTime = Date.now() - startTime;

      expect(submitTime).toBeLessThan(5000); // Should submit within 5 seconds
    });
  });
});
