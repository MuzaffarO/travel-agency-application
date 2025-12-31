# Travel Agency Backend API Documentation

## Overview

This is the backend API for the Travel Agency application, built with Java 11, AWS Lambda, API Gateway, DynamoDB, and AWS Cognito. The API is deployed using EPAM Syndicate framework.

## Table of Contents

- [Architecture](#architecture)
- [Authentication](#authentication)
- [Role-Based Access Control](#role-based-access-control)
- [API Endpoints](#api-endpoints)
  - [Authentication](#authentication-endpoints)
  - [Tours](#tours-endpoints)
  - [Bookings](#bookings-endpoints)
  - [Users](#users-endpoints)
  - [Travel Agents Management](#travel-agents-management-endpoints)
  - [AI Chat](#ai-chat-endpoints)
- [Request/Response Formats](#requestresponse-formats)
- [Error Handling](#error-handling)
- [Deployment](#deployment)

## Architecture

- **Runtime**: Java 11
- **Framework**: AWS Lambda (serverless)
- **API Gateway**: REST API with Cognito authorizer
- **Database**: DynamoDB
- **Authentication**: AWS Cognito User Pool
- **Storage**: S3 (for documents and avatars)
- **Build Tool**: Maven
- **Dependency Injection**: Dagger 2
- **Deployment**: EPAM Syndicate

## Authentication

All protected endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer <id_token>
```

The token is obtained from the `/auth/sign-in` endpoint and contains user claims including:
- `email`: User's email address
- `sub`: User's unique identifier
- `custom:role`: User's role (CUSTOMER, TRAVEL_AGENT, ADMIN)

## Role-Based Access Control

The application supports three roles:

### CUSTOMER
- View all tours
- Book tours
- View own bookings
- Cancel own bookings
- Upload documents for own bookings
- Post reviews
- Update own profile

### TRAVEL_AGENT
- All CUSTOMER permissions
- Create tours
- Edit own tours
- Delete own tours
- View bookings for tours they created
- Confirm bookings for their tours

### ADMIN
- All TRAVEL_AGENT permissions
- Create travel agent accounts
- List all travel agents
- Delete travel agents
- View all bookings
- Manage all tours (create, edit, delete any tour)

## API Endpoints

### Authentication Endpoints

#### Sign Up
**POST** `/auth/sign-up`

Create a new user account.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response:** `200 OK`
```json
{
  "message": "User registered successfully"
}
```

**Errors:**
- `400`: Invalid input or user already exists
- `500`: Internal server error

---

#### Sign In
**POST** `/auth/sign-in`

Authenticate user and receive JWT token.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```

**Response:** `200 OK`
```json
{
  "idToken": "eyJraWQiOiJ...",
  "role": "CUSTOMER",
  "userName": "John Doe",
  "email": "user@example.com"
}
```

**Errors:**
- `401`: Invalid credentials
- `400`: Bad request

---

### Tours Endpoints

#### Get Available Tours
**GET** `/tours/available`

Get paginated list of available tours with optional filters.

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)
- `destination` (optional): Filter by destination
- `startDate` (optional): Filter by start date (ISO format: yyyy-MM-dd)
- `duration` (optional): Filter by duration (e.g., "7 days")
- `mealPlan` (optional): Filter by meal plan (BB, HB, FB, AI)
- `tourType` (optional): Filter by tour type
- `minPrice` (optional): Minimum price
- `maxPrice` (optional): Maximum price

**Response:** `200 OK`
```json
{
  "tours": [
    {
      "id": "T-1234567890",
      "name": "Paris City Break",
      "destination": "Paris",
      "imageUrl": "https://...",
      "priceFrom": 1200.0,
      "rating": 4.5,
      "reviews": 25,
      "startDate": "2025-06-01"
    }
  ],
  "totalPages": 5,
  "currentPage": 0,
  "totalElements": 50
}
```

---

#### Get Tour Details
**GET** `/tours/{tourId}`

Get detailed information about a specific tour.

**Path Parameters:**
- `tourId`: Tour identifier

**Response:** `200 OK`
```json
{
  "id": "T-1234567890",
  "name": "Paris City Break",
  "destination": "Paris",
  "startDates": ["2025-06-01", "2025-06-15"],
  "durations": ["7 days", "10 days"],
  "mealPlans": ["BB", "HB", "FB", "AI"],
  "priceFrom": 1200.0,
  "priceByDuration": {
    "7 days": 1200.0,
    "10 days": 1800.0
  },
  "mealSupplementsPerDay": {
    "BB": 0.0,
    "HB": 25.0,
    "FB": 50.0,
    "AI": 75.0
  },
  "maxAdults": 2,
  "maxChildren": 2,
  "availablePackages": 10,
  "imageUrls": ["https://..."],
  "summary": "Explore the beautiful city of Paris...",
  "accommodation": "4-star hotel",
  "hotelName": "Grand Hotel Paris",
  "hotelDescription": "Luxury hotel in the heart of Paris",
  "rating": 4.5,
  "reviews": 25,
  "tourType": "City Break"
}
```

**Errors:**
- `404`: Tour not found

---

#### Get Tour Reviews
**GET** `/tours/{tourId}/feedbacks`

Get reviews/feedbacks for a specific tour.

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)

**Response:** `200 OK`
```json
{
  "reviews": [
    {
      "id": "R-123",
      "userId": "user-sub-id",
      "userName": "John Doe",
      "rating": 5,
      "comment": "Amazing tour!",
      "createdAt": "2025-01-15T10:30:00Z"
    }
  ],
  "totalPages": 2,
  "currentPage": 0
}
```

---

#### Post Tour Review
**POST** `/tours/{tourId}/feedbacks`

**Authentication:** Required (CUSTOMER, TRAVEL_AGENT, ADMIN)

Post a review for a tour.

**Request Body:**
```json
{
  "rating": 5,
  "comment": "Amazing tour experience!"
}
```

**Response:** `201 Created`
```json
{
  "message": "Review posted successfully"
}
```

---

#### Create Tour
**POST** `/tours`

**Authentication:** Required (TRAVEL_AGENT, ADMIN)

Create a new tour.

**Request Body:**
```json
{
  "name": "Paris City Break",
  "destination": "Paris",
  "startDates": ["2025-06-01", "2025-06-15"],
  "durations": ["7 days", "10 days"],
  "mealPlans": ["BB", "HB", "FB", "AI"],
  "priceFrom": 1200.0,
  "priceByDuration": {
    "7 days": 1200.0,
    "10 days": 1800.0
  },
  "mealSupplementsPerDay": {
    "BB": 0.0,
    "HB": 25.0,
    "FB": 50.0,
    "AI": 75.0
  },
  "maxAdults": 2,
  "maxChildren": 2,
  "availablePackages": 10,
  "imageUrls": ["https://example.com/image.jpg"],
  "summary": "Explore Paris",
  "accommodation": "4-star hotel",
  "hotelName": "Grand Hotel",
  "hotelDescription": "Luxury hotel",
  "tourType": "City Break",
  "freeCancellation": "Free cancellation until 7 days before",
  "freeCancellationDaysBefore": 7
}
```

**Response:** `201 Created`
```json
{
  "tourId": "T-1234567890",
  "message": "Tour created successfully"
}
```

**Errors:**
- `400`: Invalid input
- `403`: Not authorized (must be TRAVEL_AGENT or ADMIN)

---

#### Update Tour
**PUT** `/tours/{tourId}`

**Authentication:** Required (TRAVEL_AGENT, ADMIN)

Update an existing tour. Only the tour creator or ADMIN can update.

**Request Body:** Same as Create Tour

**Response:** `200 OK`
```json
{
  "tourId": "T-1234567890",
  "message": "Tour updated successfully"
}
```

**Errors:**
- `403`: Not authorized (not the tour creator)
- `404`: Tour not found

---

#### Delete Tour
**DELETE** `/tours/{tourId}`

**Authentication:** Required (TRAVEL_AGENT, ADMIN)

Delete a tour. Only the tour creator or ADMIN can delete.

**Response:** `200 OK`
```json
{
  "tourId": "T-1234567890",
  "message": "Tour deleted successfully"
}
```

**Errors:**
- `403`: Not authorized (not the tour creator)
- `404`: Tour not found

---

#### Get My Tours
**GET** `/tours/my`

**Authentication:** Required (TRAVEL_AGENT, ADMIN)

Get all tours created by the authenticated travel agent.

**Response:** `200 OK`
```json
{
  "tours": [
    {
      "id": "T-1234567890",
      "name": "Paris City Break",
      "destination": "Paris",
      "imageUrl": "https://...",
      "priceFrom": 1200.0,
      "rating": 4.5,
      "reviews": 25
    }
  ]
}
```

---

#### Get Destinations
**GET** `/tours/destinations`

Get list of all available destinations.

**Response:** `200 OK`
```json
{
  "destinations": ["Paris", "London", "Rome", "Barcelona"]
}
```

---

### Bookings Endpoints

#### Create Booking
**POST** `/bookings`

**Authentication:** Required (CUSTOMER, TRAVEL_AGENT, ADMIN)

Create a new booking.

**Request Body:**
```json
{
  "tourId": "T-1234567890",
  "startDate": "2025-06-01",
  "duration": "7 days",
  "mealPlan": "BB",
  "guests": [
    {
      "firstName": "John",
      "lastName": "Doe",
      "passportNumber": "AB123456",
      "passportExpiry": "2028-12-31"
    }
  ]
}
```

**Response:** `201 Created`
```json
{
  "bookingId": "B-1234567890",
  "message": "Booking created successfully"
}
```

---

#### View Bookings
**GET** `/bookings`

**Authentication:** Required

Get bookings based on user role:
- **CUSTOMER**: Own bookings
- **TRAVEL_AGENT**: Bookings for their tours
- **ADMIN**: All bookings

**Response:** `200 OK`
```json
{
  "bookings": [
    {
      "bookingId": "B-1234567890",
      "tourId": "T-1234567890",
      "tourName": "Paris City Break",
      "destination": "Paris",
      "startDate": "2025-06-01",
      "duration": "7 days",
      "mealPlan": "BB",
      "status": "PENDING",
      "guests": [
        {
          "firstName": "John",
          "lastName": "Doe"
        }
      ],
      "totalPrice": 1200.0,
      "agentEmail": "agent@agency.com",
      "agentName": "Travel Agent",
      "documents": []
    }
  ]
}
```

---

#### Update Booking
**PATCH** `/bookings/{bookingId}`

**Authentication:** Required (CUSTOMER, TRAVEL_AGENT, ADMIN)

Update booking details (e.g., guests, dates).

**Request Body:**
```json
{
  "startDate": "2025-06-15",
  "duration": "10 days",
  "mealPlan": "HB",
  "guests": [...]
}
```

**Response:** `200 OK`
```json
{
  "message": "Booking updated successfully"
}
```

---

#### Cancel Booking
**DELETE** `/bookings/{bookingId}`

**Authentication:** Required (CUSTOMER, TRAVEL_AGENT, ADMIN)

Cancel a booking.

**Response:** `200 OK`
```json
{
  "message": "Booking cancelled successfully"
}
```

---

#### Confirm Booking
**POST** `/bookings/{bookingId}/confirm`

**Authentication:** Required (TRAVEL_AGENT, ADMIN)

Confirm a booking (change status to CONFIRMED).

**Response:** `200 OK`
```json
{
  "message": "Booking confirmed successfully"
}
```

---

#### Upload Documents
**POST** `/bookings/{bookingId}/documents`

**Authentication:** Required (CUSTOMER, TRAVEL_AGENT, ADMIN)

Upload travel documents for a booking.

**Request Body:**
```json
{
  "documents": [
    {
      "fileName": "passport.pdf",
      "fileBase64": "base64-encoded-file-content",
      "documentType": "PASSPORT"
    }
  ]
}
```

**Response:** `200 OK`
```json
{
  "message": "Documents uploaded successfully",
  "documentIds": ["DOC-123", "DOC-456"]
}
```

---

#### List Documents
**GET** `/bookings/{bookingId}/documents`

**Authentication:** Required

Get list of documents for a booking.

**Response:** `200 OK`
```json
{
  "documents": [
    {
      "documentId": "DOC-123",
      "fileName": "passport.pdf",
      "documentType": "PASSPORT",
      "uploadedAt": "2025-01-15T10:30:00Z",
      "downloadUrl": "https://..."
    }
  ]
}
```

---

#### Delete Document
**DELETE** `/bookings/{bookingId}/documents/{documentId}`

**Authentication:** Required

Delete a document from a booking.

**Response:** `200 OK`
```json
{
  "message": "Document deleted successfully"
}
```

---

### Users Endpoints

#### Get User Info
**GET** `/users/{email}`

**Authentication:** Required

Get user profile information.

**Response:** `200 OK`
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "imageUrl": "https://...",
  "role": "CUSTOMER"
}
```

---

#### Update User Name
**PUT** `/users/{email}/name`

**Authentication:** Required (self only)

Update user's first and/or last name.

**Request Body:**
```json
{
  "firstName": "Jane",
  "lastName": "Smith"
}
```

**Response:** `200 OK`
```json
{
  "message": "Name updated successfully"
}
```

---

#### Update Password
**PUT** `/users/{email}/password`

**Authentication:** Required (self only)

Update user's password.

**Request Body:**
```json
{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewPassword123!"
}
```

**Response:** `200 OK`
```json
{
  "message": "Password updated successfully"
}
```

---

#### Update User Image
**PUT** `/users/{email}/image`

**Authentication:** Required (self only)

Upload/update user profile picture.

**Request Body:**
```json
{
  "imageBase64": "base64-encoded-image"
}
```

**Response:** `200 OK`
```json
{
  "message": "Avatar updated successfully",
  "imageUrl": "https://s3.amazonaws.com/..."
}
```

---

### Travel Agents Management Endpoints

#### Create Travel Agent
**POST** `/admin/travel-agents`

**Authentication:** Required (ADMIN only)

Create a new travel agent account.

**Request Body:**
```json
{
  "email": "agent@agency.com",
  "firstName": "Agent",
  "lastName": "One",
  "role": "TRAVEL_AGENT",
  "password": "SecurePassword123!",
  "phone": "+1234567890",
  "messenger": "agent@agency.com"
}
```

**Response:** `201 Created`
```json
{
  "email": "agent@agency.com",
  "message": "Travel agent created successfully"
}
```

---

#### List Travel Agents
**GET** `/admin/travel-agents`

**Authentication:** Required (ADMIN only)

Get list of all travel agents.

**Response:** `200 OK`
```json
{
  "agents": [
    {
      "email": "agent@agency.com",
      "firstName": "Agent",
      "lastName": "One",
      "role": "TRAVEL_AGENT",
      "phone": "+1234567890",
      "messenger": "agent@agency.com",
      "createdAt": "2025-01-15T10:30:00Z",
      "createdBy": "admin@agency.com"
    }
  ]
}
```

---

#### Delete Travel Agent
**DELETE** `/admin/travel-agents/{email}`

**Authentication:** Required (ADMIN only)

Delete a travel agent account.

**Response:** `200 OK`
```json
{
  "email": "agent@agency.com",
  "message": "Travel agent deleted successfully"
}
```

---

### AI Chat Endpoints

#### Chat
**POST** `/ai/chat`

**Authentication:** Not required

Chat with AI assistant about tours and travel.

**Request Body:**
```json
{
  "message": "What are the best tours in Paris?"
}
```

**Response:** `200 OK`
```json
{
  "response": "Here are some great tours in Paris..."
}
```

---

## Request/Response Formats

### Common Headers

**Request:**
```
Content-Type: application/json
Authorization: Bearer <token>  (for protected endpoints)
```

**Response:**
```
Content-Type: application/json
Access-Control-Allow-Origin: *
```

### Date Formats

- Dates: ISO 8601 format (`yyyy-MM-dd`)
- DateTimes: ISO 8601 format (`yyyy-MM-ddTHH:mm:ssZ`)

### Meal Plan Codes

- `BB`: Breakfast
- `HB`: Half-board
- `FB`: Full-board
- `AI`: All inclusive

## Error Handling

All errors follow this format:

```json
{
  "error": "Error message description"
}
```

### HTTP Status Codes

- `200`: Success
- `201`: Created
- `400`: Bad Request (validation errors, invalid input)
- `401`: Unauthorized (missing or invalid token)
- `403`: Forbidden (insufficient permissions)
- `404`: Not Found
- `409`: Conflict (e.g., user already exists)
- `500`: Internal Server Error

## Deployment

### Prerequisites

- Java 11+
- Maven 3.6+
- EPAM Syndicate CLI
- AWS Account with appropriate permissions

### Build

```bash
cd backend/travel-backend-app
mvn clean install -DskipTests
```

### Deploy with EPAM Syndicate

```bash
syndicate build
syndicate deploy
```

### Environment Variables

Configure in `.syndicate-config-dev`:
- `target_table`: DynamoDB table for tours
- `reviews_table`: DynamoDB table for reviews
- `bookings_table`: DynamoDB table for bookings
- `travel_agent_table_name`: DynamoDB table for travel agents
- `documents_table`: DynamoDB table for documents
- `avatars_bucket`: S3 bucket for user avatars
- `booking-documents-bucket`: S3 bucket for booking documents
- `pool_name`: Cognito User Pool name
- `region`: AWS region
- `gemini_api_key`: Google Gemini API key (for AI chat)
- `gemini_model`: Gemini model name

## Database Schema

### Tours Table
- Partition Key: `tourId`
- Attributes: name, destination, startDates, durations, mealPlans, priceFrom, etc.

### Travel Agents Table
- Partition Key: `email`
- Attributes: firstName, lastName, role, phone, messenger, createdAt, createdBy

### Bookings Table
- Partition Key: `bookingId`
- Attributes: tourId, userId, startDate, duration, mealPlan, status, guests, etc.

### Reviews Table
- Partition Key: `reviewId`
- Attributes: tourId, userId, rating, comment, createdAt

## Security Notes

1. All passwords must meet Cognito requirements (uppercase, lowercase, number, special character, min 8 characters)
2. JWT tokens expire after a set time (configured in Cognito)
3. Users can only modify their own resources unless they have ADMIN role
4. Travel agents can only modify tours they created
5. S3 buckets are private; presigned URLs are used for document access

## Support

For issues or questions, please contact the development team.

