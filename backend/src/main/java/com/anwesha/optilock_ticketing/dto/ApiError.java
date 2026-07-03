package com.anwesha.optilock_ticketing.dto;

import java.time.Instant;

/**
 * Uniform error payload returned by every handler in GlobalExceptionHandler,
 * so frontend error-handling code only needs one shape to parse.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message
) {
    public static ApiError of(int status, String error, String message) {
        return new ApiError(Instant.now(), status, error, message);
    }
}
