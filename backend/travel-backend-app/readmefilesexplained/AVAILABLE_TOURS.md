# Travel Backend (Java 11)

Serverless backend for the travel search UI. Runs on **AWS Lambda (Java 11)** behind **API Gateway**, persists data in **DynamoDB**, and uses **Dagger 2** + **AWS SDK v2 Enhanced Client**.

> **Packages moved**: all sources live under `com.travelbackendapp.travelmanagement`.

---

## Structure

```
src/main/java/com/travelbackendapp/travelmanagement
├── routing/RequestRouter.java         # Maps API Gateway paths to service methods
├── service/
│   ├── ToursService.java
│   └── impl/ToursServiceImpl.java     # Business logic, sorting, pagination, extra filters
├── repository/ToursRepository.java    # DynamoDB access (Enhanced Client)
├── mapper/TourMapper.java             # Entity -> API model mapping
├── model/
│   ├── entity/TourItem.java           # DynamoDB schema
│   └── api/
│       ├── TourResponse.java
│       ├── ToursPageResponse.java
│       └── DestinationsResponse.java
└── di/
    ├── AwsModule.java
    ├── InfraModule.java
    ├── ServiceModule.java
    └── AppComponent.java
src/main/java/com/travelbackendapp
└── TravelApiHandler.java              # Lambda entrypoint
```

---

## Requirements

- **Java 11**
- DynamoDB table with **PK: `tourId` (S)** (no GSIs required)
- Lambda env vars:
    - `table_name` — DynamoDB table name (required)
    - `region` — AWS region (used by deployment tooling; SDK uses default provider chain)

---

## Data Model (`TourItem` in DynamoDB)

```json
{
  "tourId": "T-0001",
  "name": "Garden Resort & Spa",
  "destination": "Punta Cana, Dominican Republic",
  "startDate": "2025-01-04",
  "durations": ["7 days","10 days","12 days"],
  "mealPlans": ["BB","HB","FB","AI"],
  "priceFrom": 1400,
  "rating": 5.0,
  "reviews": 19,
  "freeCancellation": "2024-12-25",
  "tourType": "RESORT",
  "maxAdults": 10,
  "maxChildren": 4,
  "availablePackages": 8
}
```

---

## Endpoints

### 1) `GET /tours/available`
Returns a **paginated** list of tours matching the filters.

**Query parameters**

| Name | Type | Default | Notes |
| --- | --- | --- | --- |
| `page` | int | `1` | 1-based page index |
| `pageSize` | int | `6` | Range: 1–100 |
| `destination` | string | — | **Exact, case-sensitive** match. `"Any destination"` or blank disables this filter. |
| `startDate` | `YYYY-MM-DD` | — | Lower bound (inclusive) on `startDate` |
| `endDate` | `YYYY-MM-DD` | — | Upper bound (inclusive) on `startDate` |
| `duration` | string | — | Exact text in `durations` list (e.g., `"7 days"`) |
| `durationBucket` | enum | — | One of `1-3`, `4-7`, `8-12`, `13+` (service-side range check) |
| `mealPlan` | enum | — | One of `BB`, `HB`, `FB`, `AI` |
| `tourType` | enum | — | One of `RESORT`, `CRUISE`, `HIKE` |
| `adults` | int | `1` | Requires `maxAdults >= adults` |
| `children` | int | `0` | Requires `maxChildren >= children` |
| `guestQuantity` | JSON | — | Alternative to flat params: `{"adults":2,"children":1}` (URL-encode) |
| `sortBy` | enum | `RATING_DESC` | `RATING_DESC` \| `RATING_ASC` \| `PRICE_DESC` \| `PRICE_ASC` |

**Date semantics**
- Only `startDate` → `startDate >= value`
- Only `endDate` → `startDate <= value`
- Both present → **inclusive range** `[startDate, endDate]`
- Dates are stored as strings → always pass `YYYY-MM-DD`.

**Duration semantics**
- `duration` filters by **exact** string in the `durations` array (`contains` in DynamoDB).
- `durationBucket` keeps tours that have **any** duration in the bucket:
    - `1-3`, `4-7`, `8-12` → numeric range
    - `13+` → `>= 13`
- If both are provided, both apply (intersection).

**Response**
```json
{
  "tours": [
    {
      "id": "T-0001",
      "name": "Garden Resort & Spa",
      "destination": "Punta Cana, Dominican Republic",
      "startDate": "2025-01-04",
      "durations": ["7 days","10 days","12 days"],
      "mealPlans": ["Breakfast (BB)","Half-board (HB)","Full-board (FB)","All inclusive (AI)"],
      "price": "from $1400 for 1 person",
      "rating": 5,
      "reviews": 19,
      "freeCancelation": "2024-12-25",
      "tourType": "RESORT"
    }
  ],
  "page": 1,
  "pageSize": 6,
  "totalPages": 3,
  "totalItems": 16
}
```

---

### 2) `GET /tours/destinations`
Destination **typeahead** for the UI. Performs a case-insensitive substring match and returns **unique** destination strings.

**Query parameters**

| Name | Type | Default | Notes |
| --- | --- | --- | --- |
| `destination` | string | — | **Required**, minimum 3 characters |
| `limit` | int | `10` | Max 50 |

**Response**
```json
{
  "destinations": [
    "Punta Cana, Dominican Republic",
    "Barahona, Dominican Republic"
  ]
}
```
---

## Notable Implementation Details

- **No secondary index**: filters are applied via DynamoDB **Scan** + filter expressions; destination exact match is done in the scan filter.
- **Sorting**: service-level (rating/price, asc/desc) after data is fetched.
- **Pagination**: service-level pagination with `page`/`pageSize`.
- **Safety**: invalid ints fall back to defaults; negative guest counts are clamped to 0.
- **Logging**: request ID, path, and filters are logged; unhandled errors return `500` with a minimal JSON body.

---

## Build / Runtime

**Java 11** is required.

**Maven**
```xml
<properties>
  <maven.compiler.source>11</maven.compiler.source>
  <maven.compiler.target>11</maven.compiler.target>
</properties>
```

**Gradle (Kotlin)**
```kotlin
java {
  toolchain { languageVersion.set(JavaLanguageVersion.of(11)) }
}
```

---

## API Gateway (snippet)

Add this alongside `/tours/available`:

```json
"/tours/destinations": {
  "enable_cors": true,
  "GET": {
    "authorization_type": "NONE",
    "integration_type": "lambda",
    "lambda_name": "travel-api-handler",
    "enable_proxy": true,
    "responses": [],
    "integration_responses": [],
    "default_error_pattern": true
  }
}
```

---

## Troubleshooting

- **Empty results with a range**: ensure dates are ISO `YYYY-MM-DD` (string comparison).
- **Both dates passed but wrong order**: the service normalizes/swaps inverted ranges.
- **DynamoDB “unused ExpressionAttributeNames”**: only add attribute name mappings when the related condition is present (already handled in repo).

---