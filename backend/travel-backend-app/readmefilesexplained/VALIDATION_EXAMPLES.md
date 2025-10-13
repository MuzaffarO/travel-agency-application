# Elegant Field Validation with Bean Validation

This document demonstrates the elegant validation approach using Bean Validation (JSR-303) annotations instead of manual validation.
### ✅ New Approach (Bean Validation)
```java
// Elegant annotation-based validation
@NotBlank(message = "First name is required")
@Size(min = 2, message = "First name must be at least 2 characters long")
private String firstName;

@NotBlank(message = "Last name is required")
@Size(min = 2, message = "Last name must be at least 2 characters long")
private String lastName;

@NotBlank(message = "Email is required")
@Email(message = "Invalid email format")
private String email;

@NotBlank(message = "Password is required")
@ValidPassword
private String password;
```

## Benefits of Bean Validation

### 1. **Declarative Validation**
- Validation rules are declared directly on the fields
- Self-documenting code
- No need to write manual validation logic

### 2. **Reusable Annotations**
- Custom validators can be reused across different DTOs
- Standard annotations work consistently across the application

### 3. **Clean Separation of Concerns**
- Validation logic is separate from business logic
- Controllers handle validation, services handle business logic

### 4. **Automatic Error Message Collection**
```java
Set<ConstraintViolation<SignUpRequestDTO>> violations = validator.validate(signUpRequest);
if (!violations.isEmpty()) {
    String errorMessage = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.joining(", "));
    return createErrorResponse(400, errorMessage);
}
```

### 5. **Custom Validators**
```java
@ValidPassword
private String password;

// Custom validator with complex logic
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
public @interface ValidPassword {
    String message() default "Password must be at least 8 characters...";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

## Available Validation Annotations

### Standard Annotations
- `@NotNull` - Field cannot be null
- `@NotBlank` - String cannot be null, empty, or whitespace
- `@NotEmpty` - Collection/array cannot be null or empty
- `@Size(min=, max=)` - Size constraints
- `@Min(value)` - Minimum numeric value
- `@Max(value)` - Maximum numeric value
- `@Email` - Email format validation
- `@Pattern(regexp=)` - Regular expression validation
- `@Past` - Date must be in the past
- `@Future` - Date must be in the future

### Custom Annotations
- `@ValidPassword` - Custom password strength validation
- `@ValidPhoneNumber` - Custom phone number validation
- `@ValidAge` - Custom age validation

## Error Handling

The validation errors are automatically collected and can be:
1. **Returned as a single concatenated string**
2. **Returned as individual field errors**
3. **Mapped to specific HTTP status codes**
4. **Logged for debugging purposes**

## Integration with Dagger

The `Validator` is provided by Dagger and injected into controllers:

```java
@Inject
public AuthController(AuthService authService, ObjectMapper objectMapper, Validator validator) {
    this.authService = authService;
    this.objectMapper = objectMapper;
    this.validator = validator;
}
```

This approach is much more elegant, maintainable, and follows Java best practices for validation.

