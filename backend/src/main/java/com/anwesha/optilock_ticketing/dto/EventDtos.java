package com.anwesha.optilock_ticketing.dto;

import com.anwesha.optilock_ticketing.entity.Event;
import com.anwesha.optilock_ticketing.entity.Seat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class EventDtos {

    public record SeatDto(
            Long id,
            String seatNumber,
            String status,
            BigDecimal price,
            Integer version
    ) {
        public static SeatDto from(Seat seat) {
            return new SeatDto(
                    seat.getId(),
                    seat.getSeatNumber(),
                    seat.getStatus().name(),
                    seat.getPrice(),
                    seat.getVersion()
            );
        }
    }

    public record EventSummaryDto(
            Long id,
            String name,
            OffsetDateTime eventDate,
            String venue,
            Integer totalSeats
    ) {
        public static EventSummaryDto from(Event event) {
            return new EventSummaryDto(
                    event.getId(),
                    event.getName(),
                    event.getEventDate(),
                    event.getVenue(),
                    event.getTotalSeats()
            );
        }
    }

    public record EventDetailDto(
            Long id,
            String name,
            OffsetDateTime eventDate,
            String venue,
            Integer totalSeats,
            List<SeatDto> seats
    ) {}
}
