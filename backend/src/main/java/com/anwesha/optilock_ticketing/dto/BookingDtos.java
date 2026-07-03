package com.anwesha.optilock_ticketing.dto;

import com.anwesha.optilock_ticketing.entity.Booking;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public class BookingDtos {

    public record BookingRequest(
            @NotNull Long seatId
    ) {}

    public record BookingResponse(
            Long bookingId,
            Long seatId,
            String seatNumber,
            Long eventId,
            OffsetDateTime bookingTime,
            String status
    ) {
        public static BookingResponse from(Booking booking) {
            return new BookingResponse(
                    booking.getId(),
                    booking.getSeat().getId(),
                    booking.getSeat().getSeatNumber(),
                    booking.getSeat().getEvent().getId(),
                    booking.getBookingTime(),
                    booking.getStatus().name()
            );
        }
    }
}
