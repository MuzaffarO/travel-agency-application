/**
 * Constants for UI tests
 */

// Test selectors
export const SELECTORS = {
  // Authentication
  LOGIN: {
    EMAIL_INPUT: "#email",
    PASSWORD_INPUT: "#password",
    SIGN_IN_BUTTON: 'button:has-text("Sign in")',
    FORGOT_PASSWORD_LINK: "text=Forgot password?",
    CREATE_ACCOUNT_LINK: 'a[href="/register"]',
    ERROR_MESSAGE: '[data-testid="login-error"]',
  },

  REGISTER: {
    FIRST_NAME_INPUT: "#firstName",
    LAST_NAME_INPUT: "#lastName",
    EMAIL_INPUT: "#email",
    PASSWORD_INPUT: "#password",
    CONFIRM_PASSWORD_INPUT: "#confirmPassword",
    CREATE_ACCOUNT_BUTTON: 'button:has-text("Create an account")',
    LOGIN_LINK: 'a[href="/login"]',
    SUCCESS_TOAST: '[data-testid="success-toast"]',
  },

  // Main page
  MAIN_PAGE: {
    PAGE_TITLE: 'h1:has-text("Search for your next tour")',
    SEARCH_SECTION: '[data-testid="search-section"]',
    LOCATION_DROPDOWN: '[data-testid="location-dropdown"]',
    DATE_SELECTOR: '[data-testid="date-selector"]',
    GUESTS_SELECTOR: '[data-testid="guests-selector"]',
    MEAL_SELECTOR: '[data-testid="meal-selector"]',
    SEARCH_BUTTON: 'button:has-text("Search")',
    SORT_DROPDOWN: '[data-testid="sort-dropdown"]',
    TOUR_CARDS: '[data-testid="tour-card"]',
    LOADING_MESSAGE: "text=Loading tours...",
    ERROR_MESSAGE: '[class*="text-red"]',
    BOOK_TOUR_BUTTON: 'button:has-text("Book")',
  },

  // My Tours page
  MY_TOURS: {
    TOUR_TABS: '[data-testid="tour-tabs"]',
    ALL_TOURS_TAB: 'button:has-text("All tours")',
    BOOKED_TAB: 'button:has-text("Booked")',
    CONFIRMED_TAB: 'button:has-text("Confirmed")',
    STARTED_TAB: 'button:has-text("Started")',
    FINISHED_TAB: 'button:has-text("Finished")',
    CANCELLED_TAB: 'button:has-text("Cancelled")',
    TOUR_CARDS: '[data-testid="my-tour-card"]',
    CANCEL_BUTTON: 'button:has-text("Cancel")',
    UPLOAD_DOCS_BUTTON: 'button:has-text("Upload Documents")',
    SEND_REVIEW_BUTTON: 'button:has-text("Send Review")',
    NO_BOOKINGS_MESSAGE: "text=No bookings yet",
  },

  // Modals
  MODALS: {
    BOOKING_MODAL: '[data-testid="booking-modal"]',
    NOT_LOGGED_MODAL: '[data-testid="not-logged-modal"]',
    CANCEL_MODAL: '[data-testid="cancel-modal"]',
    UPLOAD_DOCS_MODAL: '[data-testid="upload-docs-modal"]',
    FEEDBACK_MODAL: '[data-testid="feedback-modal"]',
    CLOSE_BUTTON: '[data-testid="close-modal"]',
    CONFIRM_BUTTON: 'button:has-text("Confirm")',
    SUBMIT_BUTTON: 'button:has-text("Submit")',
  },

  // Common elements
  COMMON: {
    HEADER: "header",
    FOOTER: "footer",
    LOADING_SPINNER: '[data-testid="loading"]',
    ERROR_MESSAGE: '[data-testid="error-message"]',
    SUCCESS_TOAST: '[data-testid="success-toast"]',
    USER_MENU: '[data-testid="user-menu"]',
    LOGOUT_BUTTON: '[data-testid="logout-button"]',
  },
};

// Test URLs
export const URLS = {
  BASE: "/",
  LOGIN: "/login",
  REGISTER: "/register",
  MY_TOURS: "/my-tours",
  TOUR_DETAILS: "/tours/:id",
  REPORTS: "/reports",
};

// Test timeouts (in milliseconds)
export const TIMEOUTS = {
  SHORT: 5000,
  MEDIUM: 10000,
  LONG: 30000,
  VERY_LONG: 60000,
};

// Test delays (in milliseconds)
export const DELAYS = {
  TYPING: 100,
  NAVIGATION: 500,
  ANIMATION: 1000,
  NETWORK: 2000,
};

// Viewport sizes
export const VIEWPORTS = {
  MOBILE: { width: 375, height: 667 },
  TABLET: { width: 768, height: 1024 },
  DESKTOP: { width: 1920, height: 1080 },
  LARGE_DESKTOP: { width: 2560, height: 1440 },
};

// Browser names
export const BROWSERS = {
  CHROMIUM: "chromium",
  FIREFOX: "firefox",
  WEBKIT: "webkit",
};

// Test data patterns
export const PATTERNS = {
  EMAIL: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  PHONE: /^\+?[\d\s\-\(\)]+$/,
  PASSWORD_STRONG:
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/,
  DATE_ISO: /^\d{4}-\d{2}-\d{2}$/,
  PRICE: /^\$[\d,]+(\.\d{2})?$/,
};

// Error messages
export const ERROR_MESSAGES = {
  AUTH: {
    INVALID_CREDENTIALS:
      "Incorrect email or password. Try again or create an account.",
    EMAIL_EXISTS: "Email already exists",
    WEAK_PASSWORD: "Password must be at least 8 characters long",
    PASSWORD_MISMATCH: "Passwords do not match",
    REQUIRED_FIELD: "This field is required",
    INVALID_EMAIL: "Please enter a valid email address",
  },

  BOOKING: {
    INVALID_DATE: "Please select a valid date",
    PAST_DATE: "Start date cannot be in the past",
    INVALID_GUESTS: "Please select at least 1 adult",
    MAX_GUESTS: "Maximum 10 guests allowed",
    REQUIRED_FIELD: "This field is required",
  },

  NETWORK: {
    CONNECTION_ERROR: "Network error. Please check your connection.",
    SERVER_ERROR: "Server error. Please try again later.",
    TIMEOUT: "Request timeout. Please try again.",
    NOT_FOUND: "The requested resource was not found.",
  },

  VALIDATION: {
    REQUIRED: "This field is required",
    MIN_LENGTH: "Must be at least {min} characters",
    MAX_LENGTH: "Must be no more than {max} characters",
    INVALID_FORMAT: "Invalid format",
  },
};

// Success messages
export const SUCCESS_MESSAGES = {
  AUTH: {
    REGISTRATION_SUCCESS:
      "Your account has been created successfully. Please sign in with your details.",
    LOGIN_SUCCESS: "Welcome back!",
    LOGOUT_SUCCESS: "You have been logged out successfully.",
  },

  BOOKING: {
    BOOKING_SUCCESS: "Your booking has been confirmed!",
    CANCELLATION_SUCCESS: "Booking cancelled successfully",
    DOCUMENT_UPLOAD: "Documents uploaded successfully",
    REVIEW_SUBMITTED: "Thank you for your review!",
  },

  PROFILE: {
    UPDATE_SUCCESS: "Profile updated successfully",
    PASSWORD_CHANGED: "Password changed successfully",
  },
};

// API endpoints
export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: "/api/auth/login",
    REGISTER: "/api/auth/register",
    LOGOUT: "/api/auth/logout",
    PROFILE: "/api/auth/profile",
  },

  TOURS: {
    SEARCH: "/api/tours",
    DETAILS: "/api/tours/:id",
    LOCATIONS: "/api/tours/locations",
  },

  BOOKINGS: {
    CREATE: "/api/bookings",
    LIST: "/api/bookings",
    DETAILS: "/api/bookings/:id",
    UPDATE: "/api/bookings/:id",
    CANCEL: "/api/bookings/:id/cancel",
    DOCUMENTS: "/api/bookings/:id/documents",
    CONFIRM: "/api/bookings/:id/confirm",
  },

  REPORTS: {
    GENERATE: "/api/reports",
    DOWNLOAD: "/api/reports/:id/download",
  },
};

// HTTP status codes
export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  INTERNAL_SERVER_ERROR: 500,
  SERVICE_UNAVAILABLE: 503,
};

// Test file paths
export const TEST_FILES = {
  VALID_DOCUMENT: "./fixtures/files/test-document.pdf",
  VALID_IMAGE: "./fixtures/files/test-image.jpg",
  INVALID_FILE: "./fixtures/files/test-file.txt",
  LARGE_FILE: "./fixtures/files/large-file.pdf",
  EMPTY_FILE: "./fixtures/files/empty-file.txt",
};

// Form field limits
export const FIELD_LIMITS = {
  NAME: {
    MIN: 2,
    MAX: 50,
  },
  EMAIL: {
    MAX: 100,
  },
  PASSWORD: {
    MIN: 8,
    MAX: 128,
  },
  COMMENT: {
    MAX: 500,
  },
  SPECIAL_REQUESTS: {
    MAX: 1000,
  },
};

// Date formats
export const DATE_FORMATS = {
  ISO: "YYYY-MM-DD",
  DISPLAY: "MMM DD, YYYY",
  INPUT: "MM/DD/YYYY",
  API: "YYYY-MM-DDTHH:mm:ss.sssZ",
};

// Currency formats
export const CURRENCY = {
  USD: {
    SYMBOL: "$",
    CODE: "USD",
    DECIMAL_PLACES: 2,
  },
  EUR: {
    SYMBOL: "€",
    CODE: "EUR",
    DECIMAL_PLACES: 2,
  },
};

// Booking statuses
export const BOOKING_STATUS = {
  CREATED: "CREATED",
  CONFIRMED: "CONFIRMED",
  STARTED: "STARTED",
  FINISHED: "FINISHED",
  CANCELLED: "CANCELLED",
};

// User roles
export const USER_ROLES = {
  CUSTOMER: "CUSTOMER",
  TRAVEL_AGENT: "TRAVEL_AGENT",
  ADMIN: "ADMIN",
};

// Tour types
export const TOUR_TYPES = {
  CITY_BREAK: "City Break",
  BEACH_HOLIDAY: "Beach Holiday",
  ADVENTURE: "Adventure",
  CULTURAL: "Cultural",
  FAMILY: "Family",
  LUXURY: "Luxury",
};

// Meal plans
export const MEAL_PLANS = {
  BREAKFAST_ONLY: "Breakfast Only",
  HALF_BOARD: "Half Board",
  FULL_BOARD: "Full Board",
  ALL_INCLUSIVE: "All Inclusive",
};

// Sort options
export const SORT_OPTIONS = {
  TOP_RATED: "Top rated first",
  MOST_POPULAR: "Most popular",
  PRICE_LOW_HIGH: "Price: Low to High",
  PRICE_HIGH_LOW: "Price: High to Low",
  DURATION_SHORT_LONG: "Duration: Short to Long",
  DURATION_LONG_SHORT: "Duration: Long to Short",
};

// Test environment variables
export const ENV = {
  BASE_URL: process.env.BASE_URL || "http://localhost:3000",
  API_BASE_URL: process.env.API_BASE_URL || "http://localhost:3001/api",
  TEST_USER_EMAIL: process.env.TEST_USER_EMAIL || "test@example.com",
  TEST_USER_PASSWORD: process.env.TEST_USER_PASSWORD || "password123",
  HEADLESS: process.env.HEADLESS === "true",
  SLOW_MO: parseInt(process.env.SLOW_MO || "0"),
  TIMEOUT: parseInt(process.env.TIMEOUT || "30000"),
};

// Test tags
export const TEST_TAGS = {
  SMOKE: "@smoke",
  REGRESSION: "@regression",
  CRITICAL: "@critical",
  SLOW: "@slow",
  FLAKY: "@flaky",
  SKIP: "@skip",
};

// Accessibility standards
export const A11Y = {
  WCAG_LEVEL: "AA",
  COLOR_CONTRAST_RATIO: 4.5,
  LARGE_TEXT_CONTRAST_RATIO: 3,
  KEYBOARD_NAVIGATION: true,
  SCREEN_READER_SUPPORT: true,
};

// Performance thresholds
export const PERFORMANCE = {
  PAGE_LOAD_TIME: 3000, // 3 seconds
  FIRST_CONTENTFUL_PAINT: 1500, // 1.5 seconds
  LARGEST_CONTENTFUL_PAINT: 2500, // 2.5 seconds
  CUMULATIVE_LAYOUT_SHIFT: 0.1,
  FIRST_INPUT_DELAY: 100, // 100ms
};
