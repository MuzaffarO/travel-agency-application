# TEST PLAN

**Travel Agency Application - MVP Development**

**PID: TravelAgency-MVP-Sprint1**

---

## Related Artifacts

| Ref. | Name |
|------|------|
| 1 | Smoke Testing Checklist - Travel Agency App |
| 2 | API Test Cases - Travel Agency App |
| 3 | Test Strategy - Travel Agency App |
| 4 | Test Result Report - Sprint 1 |
| 5 | Backend & Frontend Analysis |

## Abbreviations and Acronyms

| Term | Definition |
|------|------------|
| API | Application Programming Interface |
| AWS | Amazon Web Services |
| JWT | JSON Web Token |
| QA | Quality Assurance |
| MVP | Minimum Viable Product |
| US | User Story |

---

## CONTENTS

1. INTRODUCTION...........................................................................................................3
2. SCOPE OF WORK..........................................................................................................4
   2.1 COMPONENTS AND FUNCTIONS TO BE TESTED.........................................................4
   2.2 COMPONENTS AND FUNCTIONS NOT TO BE TESTED.................................................4
   2.3 THIRD-PARTY COMPONENTS...................................................................................5
3. QUALITY AND ACCEPTANCE CRITERIA........................................................................5
4. CRITICAL SUCCESS FACTORS.....................................................................................5
5. RISK ASSESSMENT.....................................................................................................5
6. RESOURCES................................................................................................................6
   6.1 KEY PROJECT RESOURCES.....................................................................................6
   6.2 TEST TEAM............................................................................................................6
   6.3 TEST ENVIRONMENT...............................................................................................7
   6.3.1 Test tools..........................................................................................................7
7. TEST DOCUMENTATION AND DELIVERABLES..............................................................7
8. TESTING SCHEDULE...................................................................................................8

---

## 1. INTRODUCTION

This document describes the approach and methodologies used by the testing team to plan, organize and perform the testing applications of the **Travel Agency Application - MVP Development Sprint 1**.

The **Travel Agency Application** consists of a backend API system built with Java 11 and AWS Lambda services, integrated with a React TypeScript frontend for managing travel bookings, tour selection, and user authentication.

**Note:** The project consists of both backend API services and frontend React application. The backend handles authentication via AWS Cognito, tour management through DynamoDB, and booking functionality. The frontend provides user interfaces for login, registration, and tour browsing using Redux Toolkit for state management.

---

## 2. SCOPE OF WORK

### 2.1 COMPONENTS AND FUNCTIONS TO BE TESTED

| # | Application/component name | Function name | Reference |
|---|---------------------------|---------------|-----------|
| 1 | **Authentication Service** | User Login API (POST /auth/sign-in) | US_2 - User Login |
|   |                           | User Registration API (POST /auth/sign-up) | US_1 - User Registration |
|   |                           | Role Assignment (Automatic TRAVEL_AGENT assignment) | US_3 - Automatic Role Assignment |
| 2 | **Tours Service** | Available Tours API (GET /tours/available) | US_4 - Select Available Tours |
|   |                 | Tour Details API (GET /tours/{id}) | US_5 - View Tour Details |
|   |                 | Tour Search and Filtering | US_4 - Tour search functionality |
| 3 | **Booking Service** | Tour Booking API (POST /bookings) | US_6 - Tour Booking |
|   |                   | Booking Management endpoints | US_8 - Booking Management (Sprint 2) |

### 2.2 COMPONENTS AND FUNCTIONS NOT TO BE TESTED

| # | Application/component name | Function name | Reference |
|---|---------------------------|---------------|-----------|
| 1 | **Document Management** | File upload functionality | US_7 - Documents uploading (Optional - Sprint 2) |
| 2 | **Reporting System** | Automated report generation | US_10 - Automated reports (Backend - Sprint 2) |
|   |                     | Reporting Interface | US_11 - Reporting Interface (Frontend - Sprint 2) |
| 3 | **Profile Management** | Profile update functionality | US_12 - Update Profile information (Sprint 2) |
| 4 | **Payment Integration** | Payment processing | Future sprints - not in current scope |

### 2.3 THIRD-PARTY COMPONENTS

| # | Component name | Component role | Reference/Comment |
|---|----------------|----------------|-------------------|
| 1 | **AWS Cognito** | User authentication and JWT token management | Production AWS service for user auth, role assignment via DynamoDB travel-agents table |
| 2 | **AWS API Gateway** | API routing and management | Handles all API requests with CORS configuration |
| 3 | **AWS Lambda** | Backend function execution | Java 11 runtime for all API endpoints |
| 4 | **AWS DynamoDB** | Database storage | Tours data and user role management |
| 5 | **AWS CloudFront** | CDN distribution | Performance optimization for API responses |

---

## 3. QUALITY AND ACCEPTANCE CRITERIA

• The product should work according to the requirements and functional specification listed at sections Scope of work, References: US_1 to US_6 for Sprint 1

• The product bug level should reach the acceptance criteria, and the product should not have bugs with severity Critical and Major to be released for production

---

## 4. CRITICAL SUCCESS FACTORS

**Note:** Critical factors for Sprint 1 MVP success:

• Meet schedule and complete development and testing of all Sprint 1 functionality (US_1 to US_6)

• Support backend API functionality with AWS integration

• Application shouldn't have known bugs with severity Critical and Major at the time of Sprint 1 completion

• Functional requirements do not have last minute changes during Sprint 1

• Maintain 100% backend API test pass rate

• Establish framework foundation for Sprint 2 expansion

---

## 5. RISK ASSESSMENT

**Note:** All project risks including testing ones are tracked and monitored through project management tools and documentation.

| # | Risk | Probability, % | Status | Impact | Preventive Actions | Contingency Plan |
|---|------|----------------|--------|--------|-------------------|------------------|
| 1 | AWS Environment Instability | 10 | Mitigated | Medium | Use production endpoints, maintain backup test data | Switch to staging environment if needed |
| 2 | API Changes During Testing | 20 | Managed | Medium | Verify against real implementation, maintain test documentation | Update tests to match final implementation |
| 3 | Limited QA Resources | 15 | Resolved | Low | Single QA engineer proven effective for Sprint 1 scope | Document processes for knowledge transfer |
| 4 | Sprint 2 Scope Expansion | 25 | Planned | Medium | Framework designed for scalability | Prioritize core functionality, defer optional features |
| 5 | UI Testing Gap | 30 | Acknowledged | High | Focus on backend API foundation in Sprint 1 | Implement UI automation in Sprint 2 |

---

## 6. RESOURCES

### 6.1 KEY PROJECT RESOURCES

**Note:** Key persons for the project

| # | Project Role | Name, e-mail, location |
|---|--------------|------------------------|
| 1 | Project Manager | [To be assigned] |
| 2 | Project Coordinator, Key Developer | Development Team |
| 3 | Test Leader | QA Team 3 - Run 15, Sprint 1 Lead |

### 6.2 TEST TEAM

**Note:** All test team members with their location and responsibilities

| # | Project Role | Name | Location | Responsibilities |
|---|--------------|------|----------|------------------|
| 1 | QA Lead | Team 3 - Run 15 | Remote | Sprint 1 test planning, execution, and reporting |
| 2 | QA Automation Engineer | Team 3 - Run 15 | Remote | Playwright framework, API testing, smoke tests |
| 3 | QA Documentation | Team 3 - Run 15 | Remote | Test documentation, evidence collection, reporting |

---

## 6.3 TEST ENVIRONMENT

**Note:** Production AWS environment verified and working for Sprint 1 testing

| # | Role | Resource | Hardware configuration | Software configuration |
|---|------|----------|----------------------|----------------------|
| 1 | Production API | AWS Cloud Infrastructure | AWS Lambda, API Gateway, DynamoDB | Java 11, Node.js runtime |
| 2 | Test Execution | Local Development | Standard development machine | Node 20, Playwright, GitHub Actions |
| 3 | Authentication | AWS Cognito | Managed AWS service | JWT token management, user pools |

### 6.3.1 Test tools

**Note:** All tools used for testing, tracking bugs and test documentation

| # | Tool | Comment |
|---|------|---------|
| 1 | Playwright | API and UI test automation framework |
| 2 | Node.js 20 | Runtime environment for test execution |
| 3 | GitHub Actions | CI/CD pipeline for automated test execution |
| 4 | AWS Console | Environment monitoring and verification |
| 5 | Documentation Tools | Markdown files, test evidence collection |

---

## 7. TEST DOCUMENTATION AND DELIVERABLES

**Note:** All test documentation and deliverables for Sprint 1

| # | Title | Responsible person(s) | Frequency (delivery time) | Method of delivery |
|---|-------|----------------------|---------------------------|-------------------|
| 1 | Travel Agency Test Plan | QA Team 3 - Run 15 | Once before testing start | Documentation repository |
| 2 | API Test Cases | QA Team 3 - Run 15 | Before testing start | Documentation repository |
| 3 | Smoke Testing Checklist | QA Team 3 - Run 15 | Before testing start | Documentation repository |
| 4 | Bug reports | QA Team 3 - Run 15 | Upon finding a bug | Issue tracking system |
| 5 | Test Result Reports | QA Team 3 - Run 15 | Weekly during Sprint 1 | Documentation repository |
| 6 | Sprint 1 Final Report | QA Team 3 - Run 15 | Sprint completion | Documentation repository |

---

## 8. TESTING SCHEDULE

| # | Activity | Begin Date | End Date | Assignment | Location | Work content |
|---|----------|------------|----------|------------|----------|--------------|
| 1 | Test plan creation | Sept 20, 2025 | Sept 21, 2025 | QA Team 3 - Run 15 | Remote | Test planning and documentation setup |
| 2 | Test cases creation | Sept 21, 2025 | Sept 22, 2025 | QA Team 3 - Run 15 | Remote | API test cases and smoke test implementation |
| 3 | Build installation/Environment setup | Sept 22, 2025 | Sept 23, 2025 | QA Team 3 - Run 15 | Remote | AWS environment verification and test data setup |
| 4 | Smoke Test execution | Sept 23, 2025 | Sept 24, 2025 | QA Team 3 - Run 15 | Remote | Backend API smoke testing (8 tests, 15 seconds) |
| 5 | Critical path Test execution | Sept 24, 2025 | Sept 24, 2025 | QA Team 3 - Run 15 | Remote | Full API testing and Sprint 1 completion |

---

## REVISION HISTORY

| Ver. | Description of Change | Author | Date | Approved |
|------|----------------------|--------|------|----------|
|      |                      |        |      | Name | Date |
| 0.1 | Template created | QA Team 3 - Run 15 | 10-Feb-2025 | | |
| 0.2 | Template reviewed | QA Team 3 - Run 15 | 17-Feb-2025 | | |
| 1.0 | Travel Agency Test Plan - Sprint 1 | QA Team 3 - Run 15 | Sept 24, 2025 | | |
| 2.0 | Sprint 1 Completion Update | QA Team 3 - Run 15 | Sept 24, 2025 | | |

**CONFIDENTIAL**