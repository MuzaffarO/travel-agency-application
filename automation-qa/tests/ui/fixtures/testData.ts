export interface UserData {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  role?: "CUSTOMER" | "TRAVEL_AGENT";
}

export interface TourData {
  id: string;
  name: string;
  destination: string;
  startDate: string;
  durations: string[];
  mealPlans: string[];
  price: string;
  rating: number;
  reviews: number;
  imageUrl: string;
  freeCancelation: string;
  tourType: string;
}

export interface BookingData {
  tourId: string;
  startDate: string;
  endDate: string;
  adults: number;
  children: number;
  mealPlan: string;
  specialRequests?: string;
}

export interface SearchFilters {
  location?: string;
  startDate?: string;
  endDate?: string;
  adults?: number;
  children?: number;
  mealPlan?: string;
  tourType?: string;
}

// Test Users
export const testUsers: Record<string, UserData> = {
  validCustomer: {
    firstName: "John",
    lastName: "Doe",
    email: "customer@test.com",
    password: "SecurePass123!",
    role: "CUSTOMER",
  },
  validTravelAgent: {
    firstName: "Jane",
    lastName: "Smith",
    email: "agent@test.com",
    password: "AgentPass123!",
    role: "TRAVEL_AGENT",
  },
  newUser: {
    firstName: "New",
    lastName: "User",
    email: `newuser${Date.now()}@test.com`,
    password: "NewUserPass123!",
  },
  existingUser: {
    firstName: "Existing",
    lastName: "User",
    email: "existing@test.com",
    password: "ExistingPass123!",
  },
};

// Invalid test data for negative testing
export const invalidUsers = {
  weakPassword: {
    firstName: "Test",
    lastName: "User",
    email: "test@example.com",
    password: "123",
  },
  invalidEmail: {
    firstName: "Test",
    lastName: "User",
    email: "invalid-email",
    password: "ValidPass123!",
  },
  emptyFields: {
    firstName: "",
    lastName: "",
    email: "",
    password: "",
  },
  mismatchedPasswords: {
    firstName: "Test",
    lastName: "User",
    email: "test@example.com",
    password: "Password123!",
    confirmPassword: "DifferentPassword123!",
  },
};

// Sample tour data for testing
export const sampleTours: TourData[] = [
  {
    id: "tour-1",
    name: "Amazing Paris Adventure",
    destination: "Paris, France",
    startDate: "2025-06-15",
    durations: ["7 days", "10 days"],
    mealPlans: ["All Inclusive", "Half Board"],
    price: "from $1,299 for 1 person",
    rating: 4.8,
    reviews: 156,
    imageUrl: "/images/paris-tour.jpg",
    freeCancelation: "2025-06-08",
    tourType: "City Break",
  },
  {
    id: "tour-2",
    name: "Tropical Bali Escape",
    destination: "Bali, Indonesia",
    startDate: "2025-07-20",
    durations: ["5 days", "7 days", "14 days"],
    mealPlans: ["Breakfast Only", "Half Board", "All Inclusive"],
    price: "from $899 for 1 person",
    rating: 4.9,
    reviews: 203,
    imageUrl: "/images/bali-tour.jpg",
    freeCancelation: "2025-07-13",
    tourType: "Beach Holiday",
  },
  {
    id: "tour-3",
    name: "Swiss Alps Adventure",
    destination: "Swiss Alps, Switzerland",
    startDate: "2025-08-10",
    durations: ["5 days", "7 days"],
    mealPlans: ["Half Board", "Full Board"],
    price: "from $1,599 for 1 person",
    rating: 4.7,
    reviews: 89,
    imageUrl: "/images/swiss-tour.jpg",
    freeCancelation: "2025-08-03",
    tourType: "Adventure",
  },
];

// Search filter combinations for testing
export const searchFilters: Record<string, SearchFilters> = {
  basicSearch: {
    location: "Paris",
    adults: 2,
  },
  advancedSearch: {
    location: "Bali",
    startDate: "2025-07-20",
    endDate: "2025-07-27",
    adults: 2,
    children: 1,
    mealPlan: "All Inclusive",
  },
  familySearch: {
    adults: 2,
    children: 2,
    mealPlan: "All Inclusive",
  },
  coupleSearch: {
    adults: 2,
    children: 0,
    mealPlan: "Half Board",
  },
  soloSearch: {
    adults: 1,
    children: 0,
  },
};

// Booking test data
export const bookingTestData: Record<string, BookingData> = {
  standardBooking: {
    tourId: "tour-1",
    startDate: "2025-06-15",
    endDate: "2025-06-22",
    adults: 2,
    children: 0,
    mealPlan: "Half Board",
  },
  familyBooking: {
    tourId: "tour-2",
    startDate: "2025-07-20",
    endDate: "2025-07-27",
    adults: 2,
    children: 2,
    mealPlan: "All Inclusive",
    specialRequests: "Need connecting rooms for family",
  },
  soloBooking: {
    tourId: "tour-3",
    startDate: "2025-08-10",
    endDate: "2025-08-15",
    adults: 1,
    children: 0,
    mealPlan: "Half Board",
  },
};

// Form validation test data
export const formValidationData = {
  email: {
    valid: [
      "test@example.com",
      "user.name@domain.co.uk",
      "user+tag@example.org",
    ],
    invalid: ["invalid-email", "@domain.com", "user@", "user@domain", ""],
  },
  password: {
    valid: ["SecurePass123!", "MyP@ssw0rd", "Test123$"],
    invalid: ["123", "password", "PASSWORD", "Pass123", "Pass!@#", ""],
  },
  names: {
    valid: ["John", "Mary-Jane", "José", "O'Connor"],
    invalid: ["", "123", "A", "VeryLongNameThatExceedsTheMaximumAllowedLength"],
  },
};

// Error messages for validation
export const errorMessages = {
  auth: {
    invalidCredentials:
      "Incorrect email or password. Try again or create an account.",
    emailExists: "Email already exists",
    weakPassword: "Password must be at least 8 characters long",
    passwordMismatch: "Passwords do not match",
    requiredField: "This field is required",
  },
  booking: {
    invalidDate: "Please select a valid date",
    pastDate: "Start date cannot be in the past",
    invalidGuests: "Please select at least 1 adult",
    maxGuests: "Maximum 10 guests allowed",
  },
  network: {
    connectionError: "Network error. Please check your connection.",
    serverError: "Server error. Please try again later.",
    timeout: "Request timeout. Please try again.",
  },
};

// Success messages
export const successMessages = {
  auth: {
    registrationSuccess:
      "Your account has been created successfully. Please sign in with your details.",
    loginSuccess: "Welcome back!",
  },
  booking: {
    bookingSuccess: "Your booking has been confirmed!",
    cancellationSuccess: "Booking cancelled successfully",
    documentUpload: "Documents uploaded successfully",
    reviewSubmitted: "Thank you for your review!",
  },
};

// Test environment configuration
export const testConfig = {
  timeouts: {
    short: 5000,
    medium: 10000,
    long: 30000,
  },
  retries: {
    flaky: 2,
    stable: 1,
  },
  delays: {
    typing: 100,
    navigation: 500,
    animation: 1000,
  },
};

// File paths for upload testing
export const testFiles = {
  validDocument: "./fixtures/files/test-document.pdf",
  validImage: "./fixtures/files/test-image.jpg",
  invalidFile: "./fixtures/files/test-file.txt",
  largeFile: "./fixtures/files/large-file.pdf",
};

// Responsive breakpoints for testing
export const breakpoints = {
  mobile: { width: 375, height: 667 },
  tablet: { width: 768, height: 1024 },
  desktop: { width: 1920, height: 1080 },
  largeDesktop: { width: 2560, height: 1440 },
};

// Browser configurations
export const browserConfigs = {
  chrome: {
    name: "chromium",
    viewport: breakpoints.desktop,
  },
  firefox: {
    name: "firefox",
    viewport: breakpoints.desktop,
  },
  safari: {
    name: "webkit",
    viewport: breakpoints.desktop,
  },
};

// API endpoints for mocking
export const apiEndpoints = {
  auth: {
    login: "/api/auth/login",
    register: "/api/auth/register",
    profile: "/api/auth/profile",
  },
  tours: {
    search: "/api/tours",
    details: "/api/tours/:id",
  },
  bookings: {
    create: "/api/bookings",
    list: "/api/bookings",
    update: "/api/bookings/:id",
    cancel: "/api/bookings/:id/cancel",
    documents: "/api/bookings/:id/documents",
  },
};

// Mock API responses
export const mockResponses = {
  tours: {
    success: {
      tours: sampleTours,
      page: 1,
      pageSize: 10,
      totalPages: 1,
      totalItems: 3,
    },
    empty: {
      tours: [],
      page: 1,
      pageSize: 10,
      totalPages: 0,
      totalItems: 0,
    },
  },
  auth: {
    loginSuccess: {
      token: "mock-jwt-token",
      user: {
        id: "user-123",
        email: "test@example.com",
        firstName: "Test",
        lastName: "User",
        role: "CUSTOMER",
      },
    },
    registerSuccess: {
      message: "User registered successfully",
    },
  },
};
