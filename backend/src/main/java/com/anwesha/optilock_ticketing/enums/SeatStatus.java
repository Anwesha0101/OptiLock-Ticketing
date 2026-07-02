package com.anwesha.optilock_ticketing.enums;

/**
 * Lifecycle status of a single {@code Seat}.
 *
 * AVAILABLE -> LOCKED  : a checkout attempt is in-flight (optional soft lock)
 * LOCKED    -> BOOKED  : the transactional checkout committed successfully
 * LOCKED    -> AVAILABLE: the checkout failed / expired and the seat is released
 */
public enum SeatStatus {
    AVAILABLE,
    LOCKED,
    BOOKED
}
