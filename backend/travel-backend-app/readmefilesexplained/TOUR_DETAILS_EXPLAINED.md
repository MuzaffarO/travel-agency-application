# Tour Details API (Java 11, AWS Lambda)

This document describes the **Tour Details** endpoint exposed by the Travel backend (Java 11) running on **AWS Lambda** behind **API Gateway**, with data stored in **DynamoDB** and DI via **Dagger 2**.

> All sources live under the package root `com.travelbackendapp.travelmanagement`.

---

## Endpoint

### `GET /tours/{id}`
Returns **detailed information** about a single tour (selected from search results).

- **Path params**
    - `id` (string) — unique tour identifier, e.g. `T-201`.

- **Auth**: Not required (public).
- **Caching**: none (UI may cache as needed).
- **Content-Type**: `application/json`

---

## Response Model

```jsonc
{
  "id": "T-201",
  "name": "Dolomites: 7-day guided hike",
  "destination": "Dolomites Mountains, Italy",
  "rating": 4.73,
  "reviews": 128,
  "imageUrls": ["https://example.com/image1.jpg", "https://example.com/image2.jpg"],
  "summary": "Short marketing paragraph about the tour.",
  "freeCancellationDaysBefore": 10,
  "durations": ["7 days", "10 days", "12 days"],
  "accommodation": "Plain-text accommodation description…",
  "hotelName": "Hotel Example ****",
  "hotelDescription": "Hotel features, location, amenities…",
  "mealPlans": ["Breakfast (BB)", "Half-board (HB)", "Full-board (FB)", "All inclusive (AI)"],
  "customDetails": {
    "Tourguide": "English, Italian",
    "Transfer": "Organized transfer from Milan airport.",
    "Small group": "Maximum number of participants: 8"
  },
  "startDates": ["2025-01-15", "2025-01-22", "2025-01-29"],
  "guestQuantity": {
    "adultsMaxValue": 8,
    "childrenMaxValue": 0,
    "totalMaxValue": 8
  },
  "price": {
    "7 days": "$1400",
    "10 days": "$1850",
    "12 days": "$2190"
  },
  "mealSupplementsPerDay": {
    "BB": "$0",
    "HB": "$25",
    "FB": "$40",
    "AI": "$70"
  }
}
```

### Field Notes
- **`rating` / `reviews`**: Aggregates from customer feedback. `rating` may be fractional.
- **`imageUrls`**: At least one URL; UI should handle empty list gracefully.
- **`freeCancellationDaysBefore`**: Number of days prior to the selected **start date** a booking can be cancelled without penalty.
    - When the UI preselects a start date, it can compute the **calendar deadline** as `startDate - freeCancellationDaysBefore` (ISO `YYYY-MM-DD`).
- **`durations`**: Supported durations for this tour (strings like `"7 days"`).
- **`accommodation`**: General text for non-hotel tours; if a hotel is used, `hotelName` and `hotelDescription` may be provided as well.
- **`mealPlans`**: Human-readable labels; map from codes as follows:
    - `BB` → `Breakfast (BB)`, `HB` → `Half-board (HB)`, `FB` → `Full-board (FB)`, `AI` → `All inclusive (AI)`
- **`customDetails`**: Arbitrary key-value pairs rendered as highlights (e.g., languages, transfers, group size).
- **`startDates`**: All **future** departures available for this product.
- **`guestQuantity`**: Maximums enforced by the product (adults/children/total). UI should validate selections before booking.
- **`price`**: A map of **duration → starting price per person** (strings with currency for display). Numeric prices are stored in DynamoDB; formatting is applied in the mapper.
- **`mealSupplementsPerDay`**: Optional per-day surcharges by meal plan code (string values with currency).

---

## Examples

### cURL
```bash
curl -sS "https://<api-id>.execute-api.<region>.amazonaws.com/dev/tours/T-201"
```

### 200 OK (example)
```json
{
  "id": "T-201",
  "name": "Dolomites: 7-day guided hike",
  "destination": "Dolomites Mountains, Italy",
  "rating": 4.73,
  "reviews": 128,
  "imageUrls": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg"
  ],
  "summary": "Experience the excitement of 7 days among the wonders of the Brenta Dolomites! Unforgettable excursions, breathtaking views, 7 days of adventure in a paradise.",
  "freeCancellationDaysBefore": 10,
  "durations": ["7 days"],
  "accommodation": "During your hike in Dolomites, you will be accommodated in mountain huts...",
  "hotelName": null,
  "hotelDescription": null,
  "mealPlans": ["Breakfast (BB)", "Half-board (HB)", "Full-board (FB)"],
  "customDetails": {
    "Tourguide": "English, Italian",
    "Transfer": "Organized transfer from Milan airport.",
    "Small group": "Maximum number of participants: 8"
  },
  "startDates": ["2025-01-15", "2025-01-22", "2025-01-29"],
  "guestQuantity": {
    "adultsMaxValue": 8,
    "childrenMaxValue": 0,
    "totalMaxValue": 8
  },
  "price": {
    "7 days": "$1400"
  },
  "mealSupplementsPerDay": {
    "BB": "$0",
    "HB": "$25",
    "FB": "$40"
  }
}
```

### Errors
- **400 Bad Request** — invalid path parameter format (e.g., empty `id`).
- **404 Not Found** — tour with provided `id` does not exist or is not available.
- **500 Internal Server Error** — unexpected failure (see CloudWatch logs).

---

## Implementation Notes (Backend)

- **Routing**: `RequestRouter` maps `GET /tours/{id}` to `ToursService.getTourDetails`.
- **Service**: applies presentation mapping (humanized meal plans, price formatting) and computes derived values as needed.
- **Repository**: single-item lookup by `tourId` (DynamoDB PK). Additional related rows (e.g., extra start dates) may be joined if stored separately.
- **Availability**: only tours with `availablePackages > 0` and **future** `startDates` are considered active; this aligns with search behavior.
- **Java 11**: project compiled and deployed with Java 11 runtime.

---

## Contract Stability

The response contract is **stable** for UI integration. If the data model evolves (e.g., more fields in `customDetails`), the backend will maintain backward compatibility by adding optional fields without breaking existing ones.
