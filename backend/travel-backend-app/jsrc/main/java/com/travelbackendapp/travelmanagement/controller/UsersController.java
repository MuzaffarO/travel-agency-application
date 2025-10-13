package com.travelbackendapp.travelmanagement.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbackendapp.travelmanagement.model.api.request.ChangePasswordRequestDTO;
import com.travelbackendapp.travelmanagement.model.api.request.UpdateNameRequestDTO;
import com.travelbackendapp.travelmanagement.model.api.request.UploadAvatarRequestDTO;
import com.travelbackendapp.travelmanagement.model.api.response.UpdateProfileResponseDTO;
import com.travelbackendapp.travelmanagement.model.api.response.UserInfoResponseDTO;
import com.travelbackendapp.travelmanagement.service.AuthService;
import com.travelbackendapp.travelmanagement.util.HttpResponses;
import com.travelbackendapp.travelmanagement.util.ImageBase64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class UsersController {

    private static final Logger log = LoggerFactory.getLogger(UsersController.class);

    private final AuthService authService;
    private final ObjectMapper mapper;
    private final Validator validator;
    private final S3Client s3;
    private final S3Presigner s3Presigner;
    private final String avatarsBucket;
    private final String awsRegion;

    @Inject
    public UsersController(AuthService authService, ObjectMapper mapper, Validator validator,
                           S3Client s3,
                           S3Presigner s3Presigner,
                           @Named("AVATARS_BUCKET") String avatarsBucket,
                           @Named("AWS_REGION") String awsRegion) {
        this.authService = authService;
        this.mapper = mapper;
        this.validator = validator;
        this.s3 = s3;
        this.s3Presigner = s3Presigner;
        this.avatarsBucket = avatarsBucket;
        this.awsRegion = awsRegion;
    }

    // ====== GET /users/{id} ======
    public APIGatewayProxyResponseEvent getUser(APIGatewayProxyRequestEvent event, Context ctx, String rawId) {
        try {
            Authz a = authz(event);
            if (a == null) return HttpResponses.error(mapper, 401, "Unauthorized");

            String pathEmail = urlDecode(rawId);
            if (!isSelf(pathEmail, a)) return HttpResponses.error(mapper, 403, "Forbidden");

            UserInfoResponseDTO dto = authService.getUserInfo(pathEmail);
            if (dto == null) return HttpResponses.error(mapper, 404, "User not found");

            return HttpResponses.json(mapper, 200, dto);
        } catch (Exception e) {
            log.error("GET /users/{} failed", rawId, e);
            return HttpResponses.error(mapper, 500, "Internal server error");
        }
    }



    // ====== PUT /users/{id}/name ======
    public APIGatewayProxyResponseEvent updateUserName(APIGatewayProxyRequestEvent event, Context ctx, String rawId) {
        try {
            Authz a = authz(event);
            if (a == null) return HttpResponses.error(mapper, 401, "Unauthorized");

            String pathEmail = urlDecode(rawId);
            if (!isSelf(pathEmail, a)) return HttpResponses.error(mapper, 403, "Forbidden");

            if (event.getBody() == null || event.getBody().trim().isEmpty()) {
                return HttpResponses.error(mapper, 400, "Request body is required");
            }

            UpdateNameRequestDTO body = mapper.readValue(event.getBody(), UpdateNameRequestDTO.class);

            // At least one of firstName/lastName must be present and non-blank
            boolean hasFirst = body.getFirstName() != null && !body.getFirstName().trim().isEmpty();
            boolean hasLast  = body.getLastName()  != null && !body.getLastName().trim().isEmpty();
            if (!hasFirst && !hasLast) {
                return HttpResponses.error(mapper, 400, "firstName or lastName is required");
            }

            // Bean Validation for provided fields (format/length)
            Set<ConstraintViolation<UpdateNameRequestDTO>> violations = validator.validate(body);
            if (!violations.isEmpty()) {
                String msg = violations.stream().map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));
                return HttpResponses.error(mapper, 400, msg);
            }

            // Call the dedicated service
            var resp = authService.updateUserName(pathEmail, body);
            // updateUserName returns UpdateNameResponseDTO { message }
            // Treat non-error message as 200; adjust if you prefer a success flag
            int code = "Your account has been updated successfully".equals(resp.getMessage()) ? 200 : 400;
            return HttpResponses.json(mapper, code, Map.of("message", resp.getMessage()));

        } catch (Exception e) {
            log.error("PUT /users/{}/name failed", rawId, e);
            return HttpResponses.error(mapper, 500, "Internal server error");
        }
    }


    // ====== PUT /users/{id}/password ======
    public APIGatewayProxyResponseEvent updatePassword(APIGatewayProxyRequestEvent event, Context ctx, String rawId) {
        try {
            Authz a = authz(event);
            if (a == null) return HttpResponses.error(mapper, 401, "Unauthorized");

            String pathEmail = urlDecode(rawId);
            if (!isSelf(pathEmail, a)) return HttpResponses.error(mapper, 403, "Forbidden");

            if (event.getBody() == null || event.getBody().trim().isEmpty()) {
                return HttpResponses.error(mapper, 400, "Request body is required");
            }

            ChangePasswordRequestDTO body = mapper.readValue(event.getBody(), ChangePasswordRequestDTO.class);

            Set<ConstraintViolation<ChangePasswordRequestDTO>> violations = validator.validate(body);
            if (!violations.isEmpty()) {
                String msg = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
                return HttpResponses.error(mapper, 400, msg);
            }

            if (body.getCurrentPassword().equals(body.getNewPassword())) {
                return HttpResponses.error(mapper, 400, "New password must be different from current password");
            }

            UpdateProfileResponseDTO resp =
                    authService.changePassword(pathEmail, body.getCurrentPassword(), body.getNewPassword());
            int code = resp.isSuccess() ? 200 : 400;
            return HttpResponses.json(mapper, code, Map.of("message", resp.getMessage()));

        } catch (Exception e) {
            log.error("PUT /users/{}/password failed", rawId, e);
            return HttpResponses.error(mapper, 500, "Internal server error");
        }
    }

    public APIGatewayProxyResponseEvent updateUserImage(APIGatewayProxyRequestEvent event, Context ctx, String rawId) {
        try {
            Authz a = authz(event);
            if (a == null) return HttpResponses.error(mapper, 401, "Unauthorized");

            String userEmail = urlDecode(rawId);
            if (!isSelf(userEmail, a)) return HttpResponses.error(mapper, 403, "Forbidden");

            if (event.getBody() == null || event.getBody().trim().isEmpty()) {
                return HttpResponses.error(mapper, 400, "Request body is required");
            }

            UploadAvatarRequestDTO body = mapper.readValue(event.getBody(), UploadAvatarRequestDTO.class);

            Set<ConstraintViolation<UploadAvatarRequestDTO>> violations = validator.validate(body);
            if (!violations.isEmpty()) {
                String msg = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
                return HttpResponses.error(mapper, 400, msg);
            }

            // Decode & validate image (PNG/JPEG/WEBP, <=5MB)
            ImageBase64.Result img = ImageBase64.decodeAndDetect(body.getImageBase64());

            // 1) If user already has a picture, try to delete old object (only if it’s in our avatars bucket)
            UserInfoResponseDTO existing = authService.getUserInfo(userEmail);
            if (existing != null && existing.getImageUrl() != null && !existing.getImageUrl().isBlank()) {
                String oldKey = tryExtractKeyFromOurBucket(existing.getImageUrl());
                if (oldKey != null) {
                    try {
                        s3.deleteObject(DeleteObjectRequest.builder()
                                .bucket(avatarsBucket)
                                .key(oldKey)
                                .build());
                        log.info("Deleted old avatar s3://{}/{}", avatarsBucket, oldKey);
                    } catch (S3Exception se) {
                        log.warn("Could not delete old avatar {}: {}", oldKey,
                                se.awsErrorDetails() != null ? se.awsErrorDetails().errorMessage() : se.getMessage());
                    }
                }
            }

            // 2) Upload new object with a deterministic key (timestamp for cache busting)
            String ts = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                    .withZone(java.time.ZoneOffset.UTC).format(Instant.now());
            String key = "users/" + userEmail + "/avatar/" + ts + "." + img.ext;

            PutObjectRequest put = PutObjectRequest.builder()
                    .bucket(avatarsBucket)
                    .key(key)
                    .contentType(img.contentType)
                    .cacheControl("max-age=31536000, immutable")
                    .build();

            s3.putObject(put, RequestBody.fromBytes(img.bytes));

            // 3) Build a permanent public URL (your bucket policy must allow public GET on this prefix)
            String publicUrl = "https://" + avatarsBucket + ".s3." + awsRegion + ".amazonaws.com/" + key;

            // 4) Save URL in Cognito "picture" attribute
            UpdateProfileResponseDTO resp = authService.updateUserPicture(userEmail, publicUrl);
            int code = resp.isSuccess() ? 200 : 400;

            // Return message and new URL for convenience
            return HttpResponses.json(mapper, code, Map.of(
                    "message", resp.getMessage(),
                    "imageUrl", publicUrl
            ));

        } catch (IllegalArgumentException bad) {
            return HttpResponses.error(mapper, 400, bad.getMessage());
        } catch (S3Exception s3e) {
            log.error("S3 error uploading avatar", s3e);
            return HttpResponses.error(mapper, 500, "Failed to upload image");
        } catch (Exception e) {
            log.error("PUT /users/{}/image failed", rawId, e);
            return HttpResponses.error(mapper, 500, "Internal server error");
        }
    }

    /** If url points to our avatars bucket, return the object key; otherwise null. */
    private String tryExtractKeyFromOurBucket(String url) {
        try {
            URI u = URI.create(url);
            // Expecting host like "<bucket>.s3.<region>.amazonaws.com"
            String host = u.getHost();              // e.g. tm3-user-avatars-dev1.s3.eu-west-3.amazonaws.com
            String path = u.getPath();              // e.g. /users/user@agency.com/avatar/20251001171839.jpg
            if (host == null || path == null) return null;
            if (!host.startsWith(avatarsBucket + ".")) return null;
            if (path.startsWith("/")) path = path.substring(1);
            return path.isEmpty() ? null : path;
        } catch (Exception e) {
            return null;
        }
    }


    // ---------- Auth helpers (use Cognito claims from API Gateway authorizer) ----------
    private static final class Authz {
        final String email;
        Authz(String email) { this.email = email; }
    }

    @SuppressWarnings("unchecked")
    private Authz authz(APIGatewayProxyRequestEvent event) {
        try {
            if (event.getRequestContext() == null || event.getRequestContext().getAuthorizer() == null) return null;
            Map<String, Object> auth = event.getRequestContext().getAuthorizer();
            Map<String, Object> claims = (Map<String, Object>) auth.get("claims");
            if (claims == null) return null;
            String email = (String) claims.get("email");
            return (email != null && !email.isBlank()) ? new Authz(email.trim()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isSelf(String pathEmail, Authz a) {
        return a != null && pathEmail != null && pathEmail.equalsIgnoreCase(a.email);
    }

    private static String urlDecode(String s) {
        return s == null ? null : URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
