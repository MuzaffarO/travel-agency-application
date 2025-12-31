package com.travelbackendapp.travelmanagement.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ImageBase64 Tests")
class ImageBase64Test {

    @Test
    @DisplayName("Should decode valid PNG base64")
    void shouldDecodeValidPngBase64() {
        // Given - minimal valid PNG base64 (1x1 transparent PNG)
        String base64Png = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

        // When
        ImageBase64.Result result = ImageBase64.decodeAndDetect(base64Png);

        // Then
        assertNotNull(result);
        assertEquals("png", result.ext);
        assertEquals("image/png", result.contentType);
        assertNotNull(result.bytes);
        assertTrue(result.bytes.length > 0);
    }

    @Test
    @DisplayName("Should decode valid JPEG base64")
    void shouldDecodeValidJpegBase64() {
        // Given - minimal valid JPEG base64
        String base64Jpeg = "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/2wBDAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwA/8A8A";

        // When
        ImageBase64.Result result = ImageBase64.decodeAndDetect(base64Jpeg);

        // Then
        assertNotNull(result);
        assertEquals("jpg", result.ext);
        assertEquals("image/jpeg", result.contentType);
        assertNotNull(result.bytes);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid base64")
    void shouldThrowIllegalArgumentExceptionForInvalidBase64() {
        // Given
        String invalidBase64 = "not-valid-base64!!!";

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            ImageBase64.decodeAndDetect(invalidBase64);
        });
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for non-image data")
    void shouldThrowIllegalArgumentExceptionForNonImageData() {
        // Given - valid base64 but not an image
        String textBase64 = "SGVsbG8gV29ybGQ="; // "Hello World" in base64

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            ImageBase64.decodeAndDetect(textBase64);
        });
    }

    @Test
    @DisplayName("Should handle base64 with data URL prefix")
    void shouldHandleBase64WithDataUrlPrefix() {
        // Given - base64 with data:image/png;base64, prefix
        String base64WithPrefix = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

        // When
        ImageBase64.Result result = ImageBase64.decodeAndDetect(base64WithPrefix);

        // Then
        assertNotNull(result);
        assertEquals("png", result.ext);
        assertNotNull(result.bytes);
    }

    @Test
    @DisplayName("Should detect WEBP format")
    void shouldDetectWebpFormat() {
        // Given - minimal valid WEBP base64
        String base64Webp = "UklGRiQAAABXRUJQVlA4IBgAAAAwAQCdASoBAAEAAwA0JaQAA3AA/vuUAAA=";

        // When
        ImageBase64.Result result = ImageBase64.decodeAndDetect(base64Webp);

        // Then
        assertNotNull(result);
        assertEquals("webp", result.ext);
        assertEquals("image/webp", result.contentType);
    }
}

