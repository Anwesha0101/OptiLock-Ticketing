package com.anwesha.optilock_ticketing.exception;

import com.anwesha.optilock_ticketing.dto.ApiError;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized error translation for the whole API.
 *
 * The handler that matters most for this project is
 * {@link #handleOptimisticLockingFailure}: it's what turns the
 * low-level Hibernate/JPA concurrency exception into a clean,
 * client-friendly HTTP 409 response, so the frontend can show
 * "someone beat you to this seat" instead of a raw 500 stack trace.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Fires when a seat's version check fails at flush/commit time -
     * i.e. exactly the "two clicks, same millisecond" scenario this
     * whole project is built to demonstrate.
     *
     * We catch both the Spring Data wrapper
     * ({@code ObjectOptimisticLockingFailureException}) and the raw
     * JPA exception ({@code OptimisticLockException}) because,
     * depending on exactly where in the transaction lifecycle the
     * conflict is detected, either one can be what actually reaches
     * this layer.
     */
    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ApiError> handleOptimisticLockingFailure(Exception ex) {
        log.warn("Optimistic locking conflict: {}", ex.getMessage());
        ApiError body = ApiError.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "Seat already reserved by another transaction. Please choose a different seat."
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(SeatNotAvailableException.class)
    public ResponseEntity<ApiError> handleSeatNotAvailable(SeatNotAvailableException ex) {
        log.info("Seat not available: {}", ex.getMessage());
        ApiError body = ApiError.of(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        ApiError body = ApiError.of(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
        ApiError body = ApiError.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        ApiError body = ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex) {

        log.warn("Database constraint violation", ex);

        ApiError body = ApiError.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "Seat already booked by another user."
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
    /**
     * Last-resort catch-all. Deliberately does NOT leak the raw exception
     * message to the client (could expose internals) - just logs it
     * server-side with the full stack trace.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        ApiError body = ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }*/

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {

        ex.printStackTrace();   // <-- IMPORTANT

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(
                        500,
                        ex.getClass().getName(),
                        ex.getMessage()
                ));
    }
}
