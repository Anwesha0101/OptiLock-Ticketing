package com.anwesha.optilock_ticketing.event;

/**
 * Raised whenever a seat's status changes as the result of a
 * committed transaction. Consumed by {@code SeatEventStreamService}
 * via an {@code @TransactionalEventListener(phase = AFTER_COMMIT)} so
 * that SSE clients are only ever told about state that is durably
 * saved - never about a change that later got rolled back.
 */
public record SeatStatusChangedEvent(
        Long eventId,
        Long seatId,
        String seatNumber,
        String previousStatus,
        String newStatus
) {}
