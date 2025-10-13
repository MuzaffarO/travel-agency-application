# Changelog
All notable API changes for the Travel Management service.

## [Sprint 2] - 2025-10-03

### Added
- **Bookings / Documents (User Story 7, optional)**
    - `POST /bookings/{id}/documents` – upload payment and per-guest documents.
    - `GET /bookings/{id}/documents` – list uploaded documents grouped by guest and payments.
    - `DELETE /bookings/{id}/documents/{documentId}` – delete a specific document.

- **Bookings / Confirmation (User Story 8)**
    - `POST /bookings/{id}/confirm` – mark booking documents as confirmed.

- **Users (User Story 12, optional)**
    - `GET /users/{id}` – fetch own profile (first/last name, avatar URL, role).
    - `PUT /users/{id}/name` – update first and/or last name.
    - `PUT /users/{id}/image` – upload avatar (`imageBase64`); returns permanent `imageUrl`.
    - `PUT /users/{id}/password` – change password with validation.

- **AI**
    - `POST /ai/chat` – AI chat endpoint (Gemini client).

### Changed
- **Bookings**
    - `GET /bookings` – response upgraded to `ViewBookingDTO`:
        - Adds `tourDetails` with `tourId`, pretty date + duration, meal plan, guests, price, and document count.
        - Adds `customerDetails.documents` grouping (payments + per-guest docs).
        - Adds `confirmedAtEpoch`, `canceledAtEpoch`, `freeCancellationUntil`, `duration`.
    - **Cancellation path change (see Breaking)** and response standardized to `CancelBookingResponse` with `refundAmount`, optional `cancellationFee`, `cancelledAt`, `message`.
        - Optional request body now supported: `CancelBookingRequest { cancellationReason, comment }`.

- **Tours Feedback**
    - Request DTO updated to `CreateReviewRequest { rate, comment, authorImageUrl? }` (replaces previous fields).
    - Response remains `ReviewResponse`.

### Deprecated
- **Tours**
    - `POST /tours/{id}/reviews` is functionally replaced by `POST /tours/{id}/feedbacks`. (Route parity depends on deployment; prefer `/feedbacks` going forward.)

### Removed / Replaced (Breaking)
- **Bookings**
    - `DELETE /bookings/{id}` (Sprint 1) **replaced** by `DELETE /bookings/{id}/cancel` (Sprint 2).
        - **Action required:** update client routes and adjust to optional `CancelBookingRequest` payload.

### Security / Auth
- All Users and Bookings endpoints require Cognito auth (`Authorization` header).
- Avatar upload enforces image type/size validation server-side.

---

## [Sprint 1] - 2025-09-18

### Added
- **Tours**
    - `GET /tours/available` – search with pagination, filters (destination, dates, duration bucket, meal plan, type, guests), and sorting.
    - `GET /tours/destinations` – autocomplete destinations (min 3 chars).
    - `GET /tours/{id}` – tour details (images, start dates, meal plans, pricing map, guest caps).
    - `GET /tours/{id}/reviews` – list reviews with sorting and pagination.
    - `POST /tours/{id}/reviews` – create a review (initial DTO).

- **Bookings**
    - `POST /bookings` – create a booking; returns `CreateBookingResponse` with price breakdown.
    - `GET /bookings` – list bookings for the authenticated user/agent (initial DTO).
    - `PATCH /bookings/{id}` – update booking (date, duration, meal plan, guests, personal details).
    - `DELETE /bookings/{id}` – cancel booking; initial cancellation response.

- **Auth**
    - `POST /auth/sign-up` – create account (Cognito), returns message status.
    - `POST /auth/sign-in` – authenticate, returns `idToken`, role, user info.

### Notes
- Sprint 1 established the base schemas: `ToursPageResponse`, `TourDetailResponse`, `CreateBookingRequest/Response`, `ViewBookingsResponse`, `CancelBookingResponse` (initial), `ReviewResponse`, and auth DTOs.
