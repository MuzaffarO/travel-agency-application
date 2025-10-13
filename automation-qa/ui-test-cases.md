# UI Test Cases Document

## Travel Agency Application

### Document Information

- **Project:** Travel Agency Application
- **Version:** 1.0
- **Date:** October 2025
- **Framework:** Playwright with TypeScript
- **Test Environment:** Chrome, Firefox, Safari

---

## 1. Test Strategy Overview

### 1.1 Scope

This document covers UI test cases for the Travel Agency web application, focusing on:

- User authentication flows
- Tour search and booking functionality
- User dashboard and booking management
- Responsive design validation
- Cross-browser compatibility

### 1.2 Test Approach

- **Page Object Model (POM)** for maintainable test structure
- **Data-driven testing** for multiple scenarios
- **Visual regression testing** for UI consistency
- **Accessibility testing** for compliance

---

## 2. Test Environment Setup

### 2.1 Prerequisites

- Node.js 18+
- Playwright framework
- Test data setup
- Environment configuration

### 2.2 Test Data

- Valid user credentials (Customer & Travel Agent)
- Invalid credentials for negative testing
- Sample tour data
- Booking test data

---

## 3. UI Test Cases

### 3.1 Authentication Module

#### TC-UI-001: User Registration - Happy Path

**Priority:** High  
**Test Type:** Functional  
**Description:** Verify successful user registration with valid data

**Preconditions:**

- Application is accessible
- Registration page is loaded

**Test Steps:**

1. Navigate to registration page
2. Enter valid first name (e.g., "John")
3. Enter valid last name (e.g., "Doe")
4. Enter valid email (e.g., "john.doe@example.com")
5. Enter valid password (meeting all requirements)
6. Confirm password with same value
7. Click "Create an account" button

**Expected Results:**

- Success toast message appears: "Your account has been created successfully"
- User is redirected to login page
- All form fields are validated correctly

**Test Data:**

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe+test@example.com",
  "password": "SecurePass123!"
}
```

---

#### TC-UI-002: User Registration - Email Already Exists

**Priority:** High  
**Test Type:** Negative  
**Description:** Verify error handling when registering with existing email

**Test Steps:**

1. Navigate to registration page
2. Fill form with valid data but existing email
3. Click "Create an account" button

**Expected Results:**

- Error message displayed: "Email already exists"
- User remains on registration page
- Form is not submitted

---

#### TC-UI-003: User Registration - Password Validation

**Priority:** High  
**Test Type:** Functional  
**Description:** Verify password validation rules

**Test Steps:**

1. Navigate to registration page
2. Enter weak password (e.g., "123")
3. Observe password validation rules
4. Enter strong password meeting all criteria

**Expected Results:**

- Password rules are displayed and updated in real-time
- Weak passwords show validation errors
- Strong passwords pass validation
- Password confirmation must match

---

#### TC-UI-004: User Login - Valid Credentials

**Priority:** High  
**Test Type:** Functional  
**Description:** Verify successful login with valid credentials

**Test Steps:**

1. Navigate to login page
2. Enter valid email
3. Enter valid password
4. Click "Sign in" button

**Expected Results:**

- User is authenticated successfully
- Travel Agent redirected to main page (/)
- Regular user redirected to my-tours page
- User session is established

---

#### TC-UI-005: User Login - Invalid Credentials

**Priority:** High  
**Test Type:** Negative  
**Description:** Verify error handling for invalid credentials

**Test Steps:**

1. Navigate to login page
2. Enter invalid email/password combination
3. Click "Sign in" button

**Expected Results:**

- Error message: "Incorrect email or password. Try again or create an account."
- User remains on login page
- Form fields show error state

---

#### TC-UI-006: Forgot Password Link

**Priority:** Medium  
**Test Type:** Functional  
**Description:** Verify forgot password functionality

**Test Steps:**

1. Navigate to login page
2. Click "Forgot password?" link

**Expected Results:**

- Forgot password flow is initiated
- User is guided through password recovery

---

### 3.2 Main Page / Tour Search Module

#### TC-UI-007: Tour Search - Default View

**Priority:** High  
**Test Type:** Functional  
**Description:** Verify default tour listing on main page

**Test Steps:**

1. Navigate to main page
2. Observe default tour listings

**Expected Results:**

- Tours are displayed in grid layout (2 columns on desktop)
- Each tour card shows: name, location, rating, date, duration, price
- Loading state is shown while fetching data
- Sort dropdown is available with options

---

#### TC-UI-008: Tour Search - Filter Functionality

**Priority:** High  
**Test Type:** Functional  
**Description:** Verify tour search with filters

**Test Steps:**

1. Navigate to main page
2. Use search filters (location, dates, guests, meal plans)
3. Click search button
4. Observe filtered results

**Expected Results:**

- Search filters are applied correctly
- Results update based on selected criteria
- Filter state is maintained during session
- No results message shown when applicable

---

#### TC-UI-009: Tour Booking - Authenticated User

**Priority:** High  
**Test Type:** Functional  
**Description:** Verify tour booking flow for logged-in user

**Preconditions:**

- User is logged in

**Test Steps:**

1. Navigate to main page
2. Select a tour
3. Click "Book Tour" button
4. Fill booking form with valid data
5. Submit booking

**Expected Results:**

- Booking modal opens with tour details
- Form accepts valid booking data
- Booking confirmation is displayed
- User is redirected appropriately

---

#### TC-UI-010: Tour Booking - Unauthenticated User

**Priority:** High  
**Test Type:** Functional  
**Description:** Verify booking attempt by non-logged user

**Preconditions:**

- User is not logged in

**Test Steps:**

1. Navigate to main page
2. Select a tour
3. Click "Book Tour" button

**Expected Results:**

- "Not logged in" modal appears
- User is prompted to login/register
- Booking process is blocked until authentication

---

#### TC-UI-011: Tour Search - Responsive Design

**Priority:** Medium  
**Test Type:** Visual  
**Description:** Verify responsive behavior on different screen sizes

**Test Steps:**

1. Load main page on desktop (1920x1080)
2. Resize to tablet view (768x1024)
3. Resize to mobile view (375x667)
4. Verify layout adaptation

**Expected Results:**

- Desktop: 2-column grid layout
- Tablet: Responsive grid adjustment
- Mobile: Single column layout
- All elements remain accessible and functional

---

### 3.3 My Tours / Dashboard Module

#### TC-UI-012: My Tours - View Bookings

**Priority:** High  
**Test Type:** Functional  
**Description:** Verify user can view their bookings

**Preconditions:**

- User is logged in
- User has existing bookings

**Test Steps:**

1. Navigate to My Tours page
2. Observe booking listings
3. Test tab filtering (All tours, Booked, Confirmed, etc.)

**Expected Results:**

- All user bookings are displayed
- Booking cards show complete information
- Tab filtering works correctly
- Status mapping is accurate (CREATED→Booked, etc.)

---

#### TC-UI-013: Booking Cancellation

**Priority:** High  
**Test Type:** Functional  
**Description:** Verify booking cancellation flow

**Preconditions:**

- User has cancellable booking

**Test Steps:**

1. Navigate to My Tours page
2. Select booking to cancel
3. Click cancel button
4. Confirm cancellation in modal
5. Verify booking status update

**Expected Results:**

- Cancel modal opens with booking details
- Cancellation confirmation required
- Booking status updates to "Cancelled"
- Success message displayed

---

#### TC-UI-014: Document Upload

**Priority:** Medium  
**Test Type:** Functional  
**Description:** Verify document upload functionality

**Test Steps:**

1. Navigate to My Tours page
2. Select booking requiring documents
3. Click "Upload Documents"
4. Upload valid document files
5. Submit upload

**Expected Results:**

- Upload modal opens
- File selection works correctly
- Valid files are accepted
- Upload progress is shown
- Success confirmation displayed

---

#### TC-UI-015: Feedback Submission

**Priority:** Medium  
**Test Type:** Functional  
**Description:** Verify tour feedback/review submission

**Preconditions:**

- User has completed booking

**Test Steps:**

1. Navigate to My Tours page
2. Select completed booking
3. Click "Send Review"
4. Fill feedback form
5. Submit review

**Expected Results:**

- Feedback modal opens
- Rating and comment fields available
- Form validation works
- Review is submitted successfully

---

### 3.4 Navigation and Layout

#### TC-UI-016: Header Navigation

**Priority:** Medium  
**Test Type:** Functional  
**Description:** Verify header navigation functionality

**Test Steps:**

1. Test logo click (home navigation)
2. Test user menu dropdown
3. Test logout functionality
4. Verify responsive header behavior

**Expected Results:**

- Logo navigates to home page
- User menu shows appropriate options
- Logout clears session and redirects
- Header adapts to screen size

---

#### TC-UI-017: Footer Links

**Priority:** Low  
**Test Type:** Functional  
**Description:** Verify footer links and information

**Test Steps:**

1. Scroll to footer
2. Test all footer links
3. Verify contact information
4. Check social media links

**Expected Results:**

- All links are functional
- External links open in new tab
- Contact information is accurate

---

### 3.5 Error Handling and Edge Cases

#### TC-UI-018: Network Error Handling

**Priority:** Medium  
**Test Type:** Error Handling  
**Description:** Verify graceful handling of network errors

**Test Steps:**

1. Simulate network disconnection
2. Attempt various operations
3. Reconnect network
4. Verify recovery behavior

**Expected Results:**

- Appropriate error messages displayed
- User is informed of connectivity issues
- Operations retry when connection restored
- No data loss occurs

---

#### TC-UI-019: Loading States

**Priority:** Medium  
**Test Type:** Visual  
**Description:** Verify loading indicators throughout application

**Test Steps:**

1. Navigate through different pages
2. Observe loading states for:
   - Tour listings
   - Booking operations
   - Form submissions
   - Data fetching

**Expected Results:**

- Loading indicators are shown during operations
- Skeleton screens or spinners appear appropriately
- Loading states don't block user interaction unnecessarily

---

#### TC-UI-020: Form Validation

**Priority:** High  
**Test Type:** Functional  
**Description:** Verify comprehensive form validation

**Test Steps:**

1. Test all forms with invalid data:
   - Empty required fields
   - Invalid email formats
   - Invalid date ranges
   - Invalid file types
2. Verify error messages
3. Test form recovery after errors

**Expected Results:**

- Client-side validation prevents invalid submissions
- Clear error messages guide user corrections
- Forms maintain state during validation
- Successful submission after correction

---

## 4. Cross-Browser Testing Matrix

| Test Case | Chrome | Firefox | Safari | Edge |
| --------- | ------ | ------- | ------ | ---- |
| TC-UI-001 | ✓      | ✓       | ✓      | ✓    |
| TC-UI-004 | ✓      | ✓       | ✓      | ✓    |
| TC-UI-007 | ✓      | ✓       | ✓      | ✓    |
| TC-UI-009 | ✓      | ✓       | ✓      | ✓    |
| TC-UI-012 | ✓      | ✓       | ✓      | ✓    |

## 5. Accessibility Testing

### 5.1 WCAG Compliance

- Keyboard navigation support
- Screen reader compatibility
- Color contrast validation
- Focus management
- ARIA labels and roles

### 5.2 Accessibility Test Cases

#### TC-UI-A001: Keyboard Navigation

**Description:** Verify full application navigation using keyboard only

**Test Steps:**

1. Navigate entire application using Tab, Enter, Escape keys
2. Verify focus indicators
3. Test form completion with keyboard
4. Verify modal interactions

**Expected Results:**

- All interactive elements are keyboard accessible
- Focus order is logical
- Focus indicators are visible
- No keyboard traps exist

---

## 6. Performance Testing

### 6.1 Page Load Performance

- Initial page load under 3 seconds
- Tour search results under 2 seconds
- Image loading optimization
- Bundle size optimization

### 6.2 Performance Test Cases

#### TC-UI-P001: Page Load Times

**Description:** Verify acceptable page load performance

**Test Steps:**

1. Measure initial page load time
2. Measure subsequent navigation times
3. Test with slow network conditions
4. Verify progressive loading

**Expected Results:**

- Initial load < 3 seconds
- Navigation < 1 second
- Graceful degradation on slow networks
- Progressive enhancement works

---

## 7. Test Execution Guidelines

### 7.1 Test Environment

- **Browsers:** Chrome (latest), Firefox (latest), Safari (latest)
- **Devices:** Desktop, Tablet, Mobile
- **Network:** Fast 3G, Regular 4G, WiFi
- **Screen Resolutions:** 1920x1080, 1366x768, 375x667

### 7.2 Test Data Management

- Use test-specific email addresses
- Clean up test data after execution
- Maintain separate test user accounts
- Use mock data for development testing

### 7.3 Reporting

- Screenshot on failure
- Video recording for complex flows
- Performance metrics collection
- Accessibility audit results

---

## 8. Automation Implementation Notes

### 8.1 Page Object Structure

```
tests/
├── pages/
│   ├── LoginPage.ts
│   ├── RegisterPage.ts
│   ├── MainPage.ts
│   ├── MyToursPage.ts
│   └── BasePage.ts
├── fixtures/
│   ├── testData.ts
│   └── users.ts
├── utils/
│   ├── helpers.ts
│   └── constants.ts
└── specs/
    ├── auth.spec.ts
    ├── tours.spec.ts
    └── bookings.spec.ts
```

### 8.2 Test Configuration

- Parallel execution enabled
- Retry on failure (2 attempts)
- Screenshot and video on failure
- HTML report generation

---

## 9. Maintenance and Updates

### 9.1 Test Maintenance Schedule

- Weekly: Review failing tests
- Monthly: Update test data
- Quarterly: Review test coverage
- Release: Update automation suite

### 9.2 Continuous Integration

- Run on every pull request
- Nightly full regression suite
- Performance benchmarking
- Accessibility scanning

---

**Document Version:** 1.0  
**Last Updated:** October 2025  
**Next Review:** November 2025
