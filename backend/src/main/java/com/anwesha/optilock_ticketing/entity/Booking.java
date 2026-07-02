package com.anwesha.optilock_ticketing.entity;

import com.anwesha.optilock_ticketing.enums.Role;
import com.anwesha.optilock_ticketing.enums.SeatStatus;
import com.anwesha.optilock_ticketing.enums.BookingStatus;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Records the fact that a {@link User} successfully reserved a
 * {@link Seat}. Created only after the optimistic-locked seat UPDATE
 * in the service layer has committed without a version conflict, so
 * the mere existence of a CONFIRMED Booking row implies the seat write
 * won the race.
 */
@Entity
@Table(name = "bookings", uniqueConstraints = {
        @UniqueConstraint(name = "uq_booking_seat", columnNames = {"seat_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // One-to-one in practice (uq_booking_seat enforces at most one
    // booking per seat) but modeled as @ManyToOne on the FK side,
    // which is the idiomatic JPA mapping for a unique-constrained FK.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false, unique = true)
    private Seat seat;

    @Column(name = "booking_time", nullable = false)
    private OffsetDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.CONFIRMED;

    @PrePersist
    protected void onCreate() {
        if (this.bookingTime == null) {
            this.bookingTime = OffsetDateTime.now();
        }
    }
}
