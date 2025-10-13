# Automatic Role Assignment Feature

## Overview

This document describes the automatic role assignment functionality implemented in the Travel Agency Backend application. The system automatically assigns user roles during the sign-up process based on a predefined list of travel agents stored in DynamoDB.

## Feature Description

### Automatic Role Assignment Logic

When a user signs up for the application, the system automatically determines their role using the following logic:

1. **Travel Agent Lookup**: The system checks if the user's email exists in the `travel-agents` DynamoDB table
2. **Role Assignment**: 
   - If found in the travel agents table → assigns the role specified in that record (`TRAVEL_AGENT` or `ADMIN`)
   - If not found → defaults to `CUSTOMER` role
3. **Cognito Integration**: The assigned role is stored as a custom attribute (`custom:role`) in AWS Cognito

### Supported User Roles

- **CUSTOMER**: Default role for regular users who sign up but are not in the travel agents list
- **TRAVEL_AGENT**: Users who can access travel agent specific features
- **ADMIN**: Users with administrative privileges

## Implementation Details

### Database Schema

#### Travel Agents Table (`travel-agents`)
```json
{
  "email": "string (Primary Key)",
  "firstName": "string",
  "lastName": "string", 
  "role": "string (TRAVEL_AGENT or ADMIN)",
  "createdAt": "string (ISO-8601 timestamp)",
  "createdBy": "string (Email of admin who created this agent)"
}
```

### Key Components

#### 1. TravelAgent Entity (`TravelAgent.java`)
- Represents a travel agent record in DynamoDB
- Contains email (primary key), personal info, role, and audit fields
- Uses DynamoDB Enhanced Client annotations

#### 2. TravelAgentRepository (`TravelAgentRepository.java`)
- Handles CRUD operations for travel agent records
- Key method: `findByEmail(String email)` - looks up agent by email
- Includes error handling for missing tables

#### 3. Enhanced AuthService (`AuthServiceImpl.java`)
- **New Method**: `determineUserRole(String email)` - core role assignment logic
- **New Method**: `getUserRoleFromCognito(AdminGetUserResponse)` - extracts role from Cognito
- **Enhanced Sign-Up**: Now includes automatic role assignment
- **Enhanced Sign-In**: Returns user role in response

#### 4. Updated Response Models
- `SignInResponseDTO` now includes `role` field
- Role information is returned to frontend after successful authentication

### AWS Infrastructure Changes

#### DynamoDB Table
- New table: `travel-agents` (configured in `deployment_resources.json`)
- Pay-per-request billing mode
- Primary key: email (String)

#### IAM Permissions
- Lambda execution role includes DynamoDB permissions for the travel-agents table
- Cognito permissions for user creation and authentication

## API Changes

### Sign-Up Response
The sign-up process now automatically assigns roles but doesn't expose the role in the response (for security).

### Sign-In Response
```json
{
  "idToken": "string",
  "role": "string (CUSTOMER|TRAVEL_AGENT|ADMIN)",
  "userName": "string", 
  "email": "string"
}
```

## Usage Examples

### 1. Regular Customer Sign-Up
```bash
POST /auth/sign-up
{
  "email": "customer@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "SecurePassword123!"
}
```
**Result**: User gets `CUSTOMER` role automatically

### 2. Travel Agent Sign-Up
```bash
POST /auth/sign-up
{
  "email": "agent@travelcompany.com",
  "firstName": "Jane",
  "lastName": "Smith", 
  "password": "SecurePassword123!"
}
```
**Result**: User gets `TRAVEL_AGENT` role (if email exists in travel-agents table)

### 3. Admin Sign-Up
```bash
POST /auth/sign-up
{
  "email": "admin@travelcompany.com",
  "firstName": "Admin",
  "lastName": "User",
  "password": "SecurePassword123!"
}
```
**Result**: User gets `ADMIN` role (if email exists in travel-agents table with ADMIN role)

## Security Considerations

### 1. Role Assignment Security
- Roles are assigned server-side during sign-up
- Role lookup is performed against a controlled DynamoDB table
- Default fallback to `CUSTOMER` role ensures security

### 2. Cognito Integration
- Roles are stored as custom attributes in Cognito
- Roles are included in ID tokens for frontend consumption
- No sensitive role information exposed in sign-up responses

### 3. Error Handling
- Graceful fallback to `CUSTOMER` role if travel agent lookup fails
- Comprehensive logging for audit trails
- Exception handling prevents system failures

## Configuration

### Environment Variables
- `TRAVEL_AGENT_TABLE`: DynamoDB table name for travel agents
- Standard Cognito configuration variables

### DynamoDB Setup
The travel-agents table is automatically created during deployment. To add travel agents:

```bash
# Example: Add a travel agent
aws dynamodb put-item \
  --table-name travel-agents \
  --item '{
    "email": {"S": "agent@company.com"},
    "firstName": {"S": "Jane"},
    "lastName": {"S": "Smith"},
    "role": {"S": "TRAVEL_AGENT"},
    "createdAt": {"S": "2024-01-15T10:00:00Z"},
    "createdBy": {"S": "admin@company.com"}
  }'
```

## Monitoring and Logging

### Key Log Messages
- `Assigning role 'X' to user: Y` - Role assignment during sign-up
- `Found user X in Travel Agent list with role: Y` - Successful agent lookup
- `User X not found in Travel Agent list, assigning CUSTOMER role` - Default role assignment
- `User X successfully signed in with role: Y` - Role retrieval during sign-in

### Error Scenarios
- Travel agent table not found → defaults to CUSTOMER
- Database connection issues → defaults to CUSTOMER
- Cognito custom attribute missing → defaults to CUSTOMER

## Testing

### Test Scenarios
1. **Customer Sign-Up**: Email not in travel agents table → CUSTOMER role
2. **Travel Agent Sign-Up**: Email in table with TRAVEL_AGENT role → TRAVEL_AGENT role
3. **Admin Sign-Up**: Email in table with ADMIN role → ADMIN role
4. **Database Error**: Travel agents table unavailable → CUSTOMER role
5. **Sign-In**: Verify role is returned in response

### Manual Testing Commands
```bash
# Test customer sign-up
curl -X POST /auth/sign-up \
  -H "Content-Type: application/json" \
  -d '{"email":"test@customer.com","firstName":"Test","lastName":"User","password":"Password123!"}'

# Test sign-in and verify role
curl -X POST /auth/sign-in \
  -H "Content-Type: application/json" \
  -d '{"email":"test@customer.com","password":"Password123!"}'
```

## Future Enhancements

### Potential Improvements
1. **Role Management API**: Endpoints to manage travel agents
2. **Role-Based Access Control**: Implement authorization middleware
3. **Audit Logging**: Enhanced logging for role changes
4. **Role Hierarchy**: Support for role inheritance
5. **Bulk Agent Import**: CSV import functionality for travel agents

### Integration Points
- Frontend role-based UI rendering
- Authorization middleware for protected endpoints
- Admin dashboard for agent management
