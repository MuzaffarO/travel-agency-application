package com.travelbackendapp.travelmanagement.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class S3DocumentsStorage {

    private static final Logger log = LoggerFactory.getLogger(S3DocumentsStorage.class);

    private final S3Client s3;
    private final String bucket;

    public S3DocumentsStorage(S3Client s3Client, String bucketName) {
        this.s3 = Objects.requireNonNull(s3Client, "s3Client");
        this.bucket = Objects.requireNonNull(bucketName, "BOOKING_DOCS_BUCKET");
    }

    /**
     * Stores a base64-encoded document in:
     *   bookings/{bookingId}/{section}/{optionalSub}/{timestamp}-{sanitizedFileName}
     * Returns the S3 key used.
     */
    public String putBase64(String bookingId,
                            String section,
                            String optionalSub,
                            String fileName,
                            String fileType,
                            String base64) {

        String safeName = sanitizeFileName(fileName);
        String key = buildKey(bookingId, section, optionalSub, safeName);

        byte[] bytes = decodeBase64(base64);
        String contentType = guessContentType(fileType, safeName);

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentDisposition("attachment; filename=\"" + safeName + "\"")
                .build();

        s3.putObject(req, RequestBody.fromBytes(bytes));

        log.info("Uploaded document to s3://{}/{}", bucket, key);
        return key;
    }

    private static byte[] decodeBase64(String b64) {
        if (b64 == null) return new byte[0];
        // tolerate accidental "data:...;base64," prefix
        String cleaned = b64;
        int comma = b64.indexOf(',');
        if (b64.regionMatches(true, 0, "data:", 0, 5) && comma > 0) {
            cleaned = b64.substring(comma + 1);
        }
        return Base64.getDecoder().decode(cleaned.getBytes(StandardCharsets.US_ASCII));
    }

    public void deleteObject(String key) {
        if (key == null || key.isEmpty()) return;
        s3.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
        log.info("Deleted document from s3://{}/{}", bucket, key);
    }

    private static String guessContentType(String type, String fileName) {
        if (type != null) {
            String t = type.trim().toLowerCase(Locale.ROOT);
            if (t.equals("pdf")) return "application/pdf";
            if (t.equals("png")) return "image/png";
            if (t.equals("jpg") || t.equals("jpeg")) return "image/jpeg";
        }
        String fn = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (fn.endsWith(".pdf")) return "application/pdf";
        if (fn.endsWith(".png")) return "image/png";
        if (fn.endsWith(".jpg") || fn.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    private static String sanitizeFileName(String name) {
        String base = (name == null || name.trim().isEmpty()) ? UUID.randomUUID().toString() : name.trim();
        // remove path-like parts and illegal S3 key chars for safety in the filename part
        base = base.replace("\\", "/");
        int idx = base.lastIndexOf('/');
        if (idx >= 0) base = base.substring(idx + 1);
        // keep simple safe chars, replace the rest with underscore
        return base.replaceAll("[^A-Za-z0-9._\\- ]", "_");
    }

    private static String buildKey(String bookingId, String section, String sub, String fileName) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("bookings/").append(bookingId).append('/');
        sb.append(section).append('/');
        if (sub != null && !sub.isBlank()) {
            sb.append(slug(sub)).append('/');
        }
        // attach a timestamp to avoid accidental overwrites
        sb.append(Instant.now().toEpochMilli()).append('-').append(fileName);
        return sb.toString();
    }

    /** Make “johnson doe” → “johnson-doe”, trim repeats, trim ends. */
    static String slug(String s) {
        String t = (s == null ? "" : s.trim().toLowerCase(Locale.ROOT));
        t = t.replaceAll("[^a-z0-9]+", "-");
        // FIX: correct regex (was "(^-+|+-+$)")
        t = t.replaceAll("(^-+|-+$)", "");
        return t.isEmpty() ? "guest" : t;
    }
}
