package com.anwesha.optilock_ticketing.exception;

/**
 * Thrown when a seat is not currently in a state that can be booked
 * (e.g. it's already BOOKED). Distinct from the optimistic-locking
 * race-condition case: this is a straightforward, non-racy business
 * rule violation, so it maps to 409 Conflict but does NOT touch
 * ObjectOptimisticLockingFailureException at all.
 */
public class SeatNotAvailableException extends RuntimeException {
    public SeatNotAvailableException(String message) {
        super(message);
    }
}
