# Authentication Service

This document describes the authentication functionality implemented using AWS Cognito for user registration.

## Features

### User Registration (`/auth/sign-up`)

**Endpoint:** `POST /auth/sign-up`

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe", 
  "email": "john.doe@example.com",
  "password": "Password123!"
}
```

**Response Codes:**
- `201` - Account created successfully
- `400` - Invalid input (validation errors)
- `409` - Email already exists
- `500` - Internal server error

**Response Body:**
```json
{
  "message": "Account created successfully"
}
```

## Validation Rules

### Email Validation
- Required field
- Must be a valid email format
- Checked for uniqueness against existing users

### Password Validation
- Required field
- Minimum 8 characters
- Must contain at least one uppercase letter
- Must contain at least one lowercase letter
- Must contain at least one digit
- Must contain at least one special character (@$!%*?&)

### Name Validation
- Both first name and last name are required
- Minimum 2 characters each

## Environment Variables

The following environment variables are automatically configured by Syndicate:

- `COGNITO_USER_POOL_ID` - AWS Cognito User Pool ID (from `${travel-user-pool.id}`)
- `COGNITO_CLIENT_ID` - AWS Cognito App Client ID (from `${travel-user-pool.client_id}`)

## AWS Resources

### Cognito User Pool Configuration
The deployment creates a Cognito User Pool with the following settings:
- **Password Policy**: Minimum 8 characters, requires uppercase, lowercase, numbers, and symbols
- **Username Attributes**: Email address
- **Auto-verified Attributes**: Email
- **Client Configuration**: No client secret, supports SRP and admin auth flows

### AWS Permissions
The Lambda execution role has the following Cognito permissions:
- `cognito-idp:AdminGetUser` - Check if user exists
- `cognito-idp:SignUp` - Create new user
- `cognito-idp:AdminCreateUser` - Admin user creation
- `cognito-idp:AdminSetUserPassword` - Set user password

Permissions are scoped to the specific User Pool resource for security.

## Error Handling

The service handles various error scenarios:
- Invalid input validation
- Duplicate email addresses
- AWS Cognito service errors
- Network and connectivity issues

All errors are logged appropriately and return user-friendly error messages.
