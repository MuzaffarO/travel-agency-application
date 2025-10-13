# Changelog

All notable UI/UX and frontend changes for the Travel Management service.

## [Sprint 2] - 2025-10-03

### Added

- **AI Assistant (optional)**

  - Implemented `AssistantWindow` component integrated with backend `/ai/chat`.
  - Display of AI and user messages with distinct styles and line breaks.

- **Bookings / Documents (US_7, optional)**

  - UI for uploading documents (payment and per-guest).
  - List of uploaded documents grouped by guests and payments.
  - Ability to delete specific documents from the interface.

- **Bookings / Confirmation (US_8)**

  - Implemented **Tour Booking Management interface** accessible from the Travel Agent dashboard.
  - Travel Agent can **view all bookings assigned to them**, including customer details and submitted documents.
  - Travel Agent's contact information is **automatically included** in each booking, as every tour is linked to a specific travel agent.
  - **Document verification:** Travel Agent can check and confirm all documents uploaded by the Customer.
  - **Booking confirmation:** After verifying documents, Travel Agent can mark the booking as **Confirmed**. Once confirmed, only cancellation is allowed; changes to guest count or meal plan are disabled.
  - **Tour status updates:** The system automatically marks a tour as **Finished** once the tour end date has been reached.
  - **Tour cancellation:** Travel Agent can cancel a scheduled tour if there is a significant reason (e.g., Customer emergency, hotel issues). Cancellation without fees is allowed **up to 10 days before the tour start date**.
  - **Secure access:** Bookings are stored securely and are accessible only to the assigned Travel Agent and the Customer.
  - **Editing bookings:** Travel Agent can edit tour details (guest quantity, meal plan, duration) **with Customer confirmation**.
  - **UI elements:** Added confirmation buttons for booking documents and real-time display of booking status in the bookings list.

- **Tour Feedback (US_9)**

  - Customers can provide feedback for tours marked as "Started".
  - Star rating is obligatory for all feedback.
  - Textual comment is **required** for 1-3 stars and **optional** for 4-5 stars.
  - After submission, feedback can be **viewed and updated** in "Finished" bookings.
  - Feedback is clearly displayed on the Tour detailed information page.
  - Display includes reviewer’s avatar, star rating, and comment.

- **Reporting Interface (US_11, optional)**

  - Added initial admin dashboard UI (user/tour/booking management stubs).
  - Routing and basic layout prepared for future backend integration.

- **User Profile Management (US_12, optional)**

  - Profile page: first/last name, avatar, password, email.
  - Functionality to update name and avatar.
  - Password change form with validation.

- **Password Recovery (US_14 frontend only)**

  - Added UI for "Forgot Password" flow.
  - Includes form validation and confirmation screens.
  - Not yet connected to backend — only client-side implementation done.

### Changed

- **Bookings List**

  - Added new fields: duration, meal plan, guest count, price, document count.
  - Display of booking statuses: `confirmed`, `canceled`, `freeCancellationUntil`.

- **Tour Feedback**

  - Updated form: now accepts `rate`, `comment`, `authorImageUrl`.
  - Display of reviewer’s avatar image.

### Deprecated

- **Bookings Endpoint**

  - Old endpoint `GET /bookings` (Sprint 1) is deprecated.
  - Replaced with the updated `GET /bookings` response that now includes guest details (first and last name for each guest).

### Removed / Replaced (Breaking)

- **Booking Cancellation UI**

  - "Cancel booking" button replaced with new `/bookings/{id}/cancel` route.

### Security / Auth

- All profile and bookings UI components now require `Authorization` token.
- Added client-side validation for avatar file type/size before upload.

---

## [Sprint 1] - 2025-09-19

### Added

- **User Profile Management**

  - **US_1 – Profile Registration**: registration form with validation.
  - **US_2 – Login**: login form, token storage, redirects.
  - **US_3 – Automatic Role Assignment**: roles (`Customer`, `Travel Agent`, `Admin`) automatically assigned after login.

- **Travel Booking Management**

  - **US_4 – Select Available Tours**: tours list with filters, search, and pagination.
  - **US_5 – View Detailed Tour Info**: tour detail page (gallery, start dates, meal plan, pricing, description).
  - **US_6 – Tour Booking**: booking form with travel dates, destination, number of guests, and accommodation selection.

### Notes

- Sprint 1 established the foundation for **authentication**, **user profile**, and **core pages for tours and bookings**.
- All UI components integrated via routing (`React Router`).
