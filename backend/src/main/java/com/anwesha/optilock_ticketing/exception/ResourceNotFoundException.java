package com.anwesha.optilock_ticketing.exception;

/**
 * Thrown when a referenced Seat, Event, User, or Booking id does not
 * exist. Translated to HTTP 404 by GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
