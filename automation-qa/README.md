# Travel Agency - QA Automation

Sprint 2 - MVP Extension (Complete API Coverage)  
Team: Team 3 - Run 15  
**Status:** ✅ **100% API ENDPOINT COVERAGE ACHIEVED**

## Overview

This directory contains the automated testing infrastructure for the Travel Agency application: documentation, test cases, and the JavaScript-based automation framework.

## Project Structure

```
automation-qa/
├── README.md                          # This file
├── test-strategy.md                   # Test Strategy
├── test-plan.md                       # Test Plan
├── api-test-cases.md                  # API Test Cases (Sprint 1 + 2)
├── smoke-checklist.md                 # Smoke Testing Checklist
├── coverage-matrix.csv                # Coverage matrix
├── test-result-report-sprint1.md      # Sprint 1 Test Result Report
├── package.json                       # NPM scripts and dependencies
├── playwright.config.ts               # Playwright configuration
├── tests/
│   ├── api/                           # API tests
│   ├── smoke/                         # Smoke tests
│   │   ├── smoke.spec.ts              # Sprint 1 core tests
│   │   ├── basic.smoke.spec.ts        # Basic smoke tests
│   │   ├── axios.smoke.spec.ts        # Auth validation
│   │   ├── sprint2-api.spec.ts        # ✅ Sprint 2 comprehensive API coverage
│   │   ├── reports-integration.spec.ts # ✅ Reports system integration
│   │   └── comprehensive-api-coverage.spec.ts # ✅ Full endpoint validation
│   └── mocks/                         # Mock API responses
└── config/
    └── test.env.example               # Example environment variables
```

## Quick Start

Prerequisites:

- Node.js 18+ (recommended 20)
- npm 9+

Installation:

```bash
cd automation-qa
npm ci
npx playwright install --with-deps
```

Run tests:

```bash
# Sprint 1 Smoke Tests
npm run test:smoke

# Sprint 2 Comprehensive API Tests
npx playwright test tests/smoke/sprint2-api.spec.ts

# Reports Integration Tests
npx playwright test tests/smoke/reports-integration.spec.ts

# Full API Coverage Validation
npx playwright test tests/smoke/comprehensive-api-coverage.spec.ts

# All smoke tests
npm run test:smoke

# All tests
npm test
```

Reports:

- Playwright HTML report in `playwright-report/`
- JUnit XML in `test-results/` (for CI)

## Configuration

Copy `config/test.env.example` to `.env` and adjust:

```env
BASE_URL=https://travel-agency-dev.example.com
API_BASE_URL=$BASE_URL/api
CUSTOMER_EMAIL=customer@test.com
CUSTOMER_PASSWORD=Password123!
AGENT_EMAIL=agent@test.com
AGENT_PASSWORD=Password123!
ADMIN_EMAIL=admin@test.com
ADMIN_PASSWORD=Password123!
```

## Scope & Coverage ✅ COMPLETE

### Sprint 1 - MVP Core (✅ Verified with Production AWS)

- US_1: User Registration ✅
- US_2: User Login ✅
- US_3: Automatic Role Assignment ✅
- US_4: Select Available Tours ✅
- US_5: View Tour Details ✅
- US_6: Tour Booking ✅

### Sprint 2 - MVP Extension (✅ Full API Coverage)

- US_7: Document Upload by Customer ✅
- US_8: Tour Booking Management by Travel Agent ✅
- US_9: Customer Feedback & Reviews ✅
- US_10: Automated Reports Generation ✅
- US_11: Reporting Interface ✅
- US_12: Update Profile Information ✅

### API Endpoints Coverage (20+ endpoints)

- ✅ Authentication: sign-in, sign-up, profile updates
- ✅ Tours: available tours, destinations, details, reviews
- ✅ Bookings: create, view, update, confirm, cancel
- ✅ Documents: upload passports/payments, list documents
- ✅ Reports: event generation, SQS integration, EventBridge
- ✅ Security: JWT authentication, role-based access

### Infrastructure Integration Tested

- ✅ AWS Lambda functions
- ✅ DynamoDB tables (tours, bookings, reviews, reports, documents)
- ✅ SQS queue for booking events
- ✅ S3 bucket for document storage
- ✅ EventBridge scheduled reports
- ✅ SES email integration (simulated)
- ✅ AWS Cognito authentication

## Troubleshooting

- 401 Unauthorized: check credentials/tokens
- Connection refused: verify environment URLs
- Slow tests: run headless or reduce retries

## Contacts

- QA Lead: [Name]
- Dev Lead: [Name]
- Product Owner: [Name]
