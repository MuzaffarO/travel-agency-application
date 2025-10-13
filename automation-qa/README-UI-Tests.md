# UI Test Automation - Travel Agency App

## Overview

This document provides comprehensive information about the UI test automation implementation for the Travel Agency application using Playwright with TypeScript.

## 🏗️ Framework Architecture

### Technology Stack

- **Framework**: Playwright
- **Language**: TypeScript
- **Pattern**: Page Object Model (POM)
- **Test Runner**: Playwright Test Runner
- **Reporting**: HTML, JUnit XML

### Project Structure

```
tests/ui/
├── pages/           # Page Object Models
│   ├── BasePage.ts
│   ├── LoginPage.ts
│   ├── RegisterPage.ts
│   ├── MainPage.ts
│   └── MyToursPage.ts
├── fixtures/        # Test data and mock files
│   ├── testData.ts
│   └── files/
├── utils/           # Helper functions and utilities
│   ├── helpers.ts
│   └── constants.ts
└── specs/           # Test specifications
    ├── auth.spec.ts
    ├── tours.spec.ts
    └── bookings.spec.ts
```

## 🚀 Getting Started

### Prerequisites

- Node.js 18+
- npm or yarn
- Playwright installed

### Installation

```bash
cd automation-qa
npm install
npx playwright install
```

### Running Tests

```bash
# Run all UI tests
npm run test:ui

# Run specific test file
npx playwright test tests/ui/specs/auth.spec.ts

# Run tests in headed mode
npx playwright test tests/ui/specs/auth.spec.ts --headed

# Run tests in debug mode
npx playwright test tests/ui/specs/auth.spec.ts --debug
```

### View Test Reports

```bash
npm run report
```

## 📋 Test Coverage

### Authentication Module (25 tests)

- **User Registration**: Happy path, validation, error handling
- **User Login**: Valid/invalid credentials, form validation
- **Session Management**: Persistent login, logout, protected routes
- **Accessibility**: Keyboard navigation, ARIA labels
- **Responsive Design**: Mobile, tablet, desktop views

### Tours and Search Module (30 tests)

- **Tour Display**: Default view, loading states, error handling
- **Search Functionality**: Basic/advanced search, filters, sorting
- **Booking Flow**: Authenticated/unauthenticated users, modals
- **Responsive Design**: Grid layouts, mobile interactions
- **Performance**: Page load times, search performance
- **Accessibility**: Keyboard navigation, screen reader support

### Bookings Management Module (25 tests)

- **My Tours Display**: Booking cards, tab navigation, status filtering
- **Booking Actions**: Cancel, upload documents, send reviews
- **Status Management**: Status-based filtering, action buttons
- **Responsive Design**: Mobile layouts, modal responsiveness
- **Performance**: Load times, tab switching, file uploads
- **Error Handling**: Network errors, validation errors

## 🎯 Test Scenarios

### Critical User Journeys

1. **User Registration → Login → Search Tours → Book Tour**
2. **Login → View My Tours → Cancel Booking**
3. **Login → View My Tours → Upload Documents**
4. **Login → View My Tours → Submit Review**

### Cross-Browser Testing

- Chrome (Chromium)
- Firefox
- Safari (WebKit)

### Responsive Testing

- Mobile: 375x667
- Tablet: 768x1024
- Desktop: 1920x1080

## 🔧 Configuration

### Playwright Configuration

```typescript
// playwright.config.ts
export default defineConfig({
  testDir: "./tests",
  projects: [
    {
      name: "UI - Chromium",
      testDir: "./tests/ui",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
});
```

### Environment Variables

```bash
BASE_URL=http://localhost:3000
API_BASE_URL=http://localhost:3001/api
TEST_USER_EMAIL=test@example.com
TEST_USER_PASSWORD=password123
HEADLESS=true
```

## 📊 Page Object Model

### Base Page

```typescript
export class BasePage {
  readonly page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  // Common methods for all pages
  async navigateTo(url: string): Promise<void>;
  async waitForPageLoad(): Promise<void>;
  async verifyElementVisible(locator: Locator): Promise<void>;
}
```

### Page-Specific Classes

- **LoginPage**: Authentication functionality
- **RegisterPage**: User registration
- **MainPage**: Tour search and booking
- **MyToursPage**: Booking management

## 🧪 Test Data Management

### Test Users

```typescript
export const testUsers = {
  validCustomer: {
    email: "customer@test.com",
    password: "SecurePass123!",
    role: "CUSTOMER",
  },
  validTravelAgent: {
    email: "agent@test.com",
    password: "AgentPass123!",
    role: "TRAVEL_AGENT",
  },
};
```

### Dynamic Data Generation

```typescript
// Generate unique test data
const userData = TestDataGenerator.generateUser();
const bookingData = TestDataGenerator.generateBooking();
```

## 🔍 Utilities and Helpers

### Authentication Utils

```typescript
await AuthUtils.loginUser(page, email, password);
await AuthUtils.logoutUser(page);
const isLoggedIn = await AuthUtils.isUserLoggedIn(page);
```

### Wait Utils

```typescript
await WaitUtils.waitForCondition(() => condition);
await WaitUtils.waitForElementToAppear(page, selector);
```

### Form Utils

```typescript
await FormUtils.fillFormField(page, selector, value);
await FormUtils.selectDropdownOption(page, dropdown, option);
```

## 📈 Reporting and Analytics

### Test Reports

- **HTML Report**: Detailed test results with screenshots
- **JUnit XML**: CI/CD integration
- **Console Output**: Real-time test execution feedback

### Screenshots and Videos

- Screenshots on test failure
- Video recording for complex flows
- Full page screenshots for visual validation

### Performance Metrics

- Page load times
- Element load times
- Network request monitoring

## 🚨 Error Handling

### Network Errors

```typescript
// Mock network failures
await page.route("**/api/tours", (route) => {
  route.abort("failed");
});
```

### Validation Errors

```typescript
// Verify form validation
await FormUtils.validateFormField(page, selector, expectedError);
```

### Retry Mechanisms

```typescript
// Retry flaky operations
await RetryUtils.retry(
  async () => {
    await someFlakOperation();
  },
  3,
  1000
);
```

## ♿ Accessibility Testing

### Keyboard Navigation

```typescript
await AccessibilityUtils.checkKeyboardNavigation(page, selectors);
```

### ARIA Labels

```typescript
await AccessibilityUtils.checkAriaLabels(page, selectors);
```

### Color Contrast

```typescript
await AccessibilityUtils.checkColorContrast(page);
```

## 🎨 Visual Testing

### Responsive Design Validation

```typescript
await BrowserUtils.setViewportSize(page, "mobile");
await mainPage.verifyResponsiveLayout("mobile");
```

### Cross-Browser Compatibility

- Automated testing across Chrome, Firefox, Safari
- Visual regression detection
- Layout consistency validation

## 🔄 CI/CD Integration

### GitHub Actions Example

```yaml
- name: Run UI Tests
  run: |
    npm run test:ui

- name: Upload Test Results
  uses: actions/upload-artifact@v2
  with:
    name: playwright-report
    path: playwright-report/
```

### Test Execution Strategy

- **Smoke Tests**: Critical user journeys (5-10 minutes)
- **Regression Tests**: Full test suite (30-45 minutes)
- **Nightly Tests**: Extended test suite with performance testing

## 📝 Best Practices

### Test Design

1. **Independent Tests**: Each test should be self-contained
2. **Data Isolation**: Use unique test data for each test
3. **Stable Selectors**: Use data-testid attributes
4. **Wait Strategies**: Explicit waits over implicit waits

### Maintenance

1. **Regular Updates**: Keep selectors and test data current
2. **Flaky Test Management**: Identify and fix unstable tests
3. **Performance Monitoring**: Track test execution times
4. **Documentation**: Keep test documentation updated

### Code Quality

1. **TypeScript**: Strong typing for better maintainability
2. **ESLint**: Code quality and consistency
3. **Code Reviews**: Peer review for test code
4. **Refactoring**: Regular cleanup and optimization

## 🐛 Debugging

### Debug Mode

```bash
npx playwright test --debug
```

### Trace Viewer

```bash
npx playwright show-trace trace.zip
```

### Console Logs

```typescript
// Enable console logging
page.on("console", (msg) => console.log(msg.text()));
```

## 📚 Resources

### Documentation

- [Playwright Documentation](https://playwright.dev/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [Testing Best Practices](https://playwright.dev/docs/best-practices)

### Training Materials

- UI Test Cases Document: `ui-test-cases.md`
- API Test Documentation: `api-test-cases.md`
- Test Strategy: `test-strategy.md`

## 🤝 Contributing

### Adding New Tests

1. Create test in appropriate spec file
2. Follow naming convention: `TC-UI-XXX`
3. Use existing page objects or create new ones
4. Add test data to fixtures
5. Update documentation

### Reporting Issues

1. Include test name and browser
2. Provide error messages and screenshots
3. Steps to reproduce
4. Expected vs actual behavior

---

**Last Updated**: October 2025  
**Version**: 1.0  
**Maintainer**: QA Team
