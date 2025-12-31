package com.travelbackendapp.travelmanagement.util;

import com.travelbackendapp.travelmanagement.exceptions.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RequestUtils Tests")
class RequestUtilsTest {

    @Test
    @DisplayName("Should return true for blank strings")
    void shouldReturnTrueForBlankStrings() {
        assertTrue(RequestUtils.isBlank(null));
        assertTrue(RequestUtils.isBlank(""));
        assertTrue(RequestUtils.isBlank("   "));
        assertFalse(RequestUtils.isBlank("test"));
        assertFalse(RequestUtils.isBlank("  test  "));
    }

    @Test
    @DisplayName("Should parse integer or return default")
    void shouldParseIntegerOrReturnDefault() {
        assertEquals(10, RequestUtils.parseIntOrDefault("10", 0));
        assertEquals(0, RequestUtils.parseIntOrDefault(null, 0));
        assertEquals(0, RequestUtils.parseIntOrDefault("", 0));
        assertEquals(0, RequestUtils.parseIntOrDefault("invalid", 0));
        assertEquals(5, RequestUtils.parseIntOrDefault("5", 10));
    }

    @Test
    @DisplayName("Should clamp value to minimum")
    void shouldClampValueToMinimum() {
        assertEquals(5, RequestUtils.clampMin(3, 5));
        assertEquals(10, RequestUtils.clampMin(10, 5));
        assertEquals(20, RequestUtils.clampMin(20, 5));
    }

    @Test
    @DisplayName("Should clamp value to range")
    void shouldClampValueToRange() {
        assertEquals(5, RequestUtils.clampRange(3, 5, 10));
        assertEquals(7, RequestUtils.clampRange(7, 5, 10));
        assertEquals(10, RequestUtils.clampRange(15, 5, 10));
    }

    @Test
    @DisplayName("Should return null or trimmed value")
    void shouldReturnNullOrTrimmedValue() {
        assertNull(RequestUtils.valOrNull(null));
        assertNull(RequestUtils.valOrNull(""));
        assertNull(RequestUtils.valOrNull("   "));
        assertEquals("test", RequestUtils.valOrNull("test"));
        assertEquals("test", RequestUtils.valOrNull("  test  "));
    }

    @Test
    @DisplayName("Should normalize valid ISO date")
    void shouldNormalizeValidIsoDate() {
        assertEquals("2025-06-01", RequestUtils.normalizeIsoDate("2025-06-01"));
        assertEquals("2025-12-31", RequestUtils.normalizeIsoDate("2025-12-31"));
    }

    @Test
    @DisplayName("Should throw BadRequestException for invalid date format")
    void shouldThrowBadRequestExceptionForInvalidDate() {
        assertThrows(BadRequestException.class, () -> RequestUtils.normalizeIsoDate("invalid-date"));
        assertThrows(BadRequestException.class, () -> RequestUtils.normalizeIsoDate("2025/06/01"));
        assertThrows(BadRequestException.class, () -> RequestUtils.normalizeIsoDate("01-06-2025"));
    }

    @Test
    @DisplayName("Should return null for null date input")
    void shouldReturnNullForNullDateInput() {
        assertNull(RequestUtils.normalizeIsoDate(null));
    }
}

