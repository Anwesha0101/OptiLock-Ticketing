package com.anwesha.optilock_ticketing.entity;

import com.anwesha.optilock_ticketing.enums.Role;
import com.anwesha.optilock_ticketing.enums.SeatStatus;
import com.anwesha.optilock_ticketing.enums.BookingStatus;


import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A bookable event (concert, match, conference, etc.) that owns a
 * collection of {@code Seat} rows generated at event-creation time.
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "event_date", nullable = false)
    private OffsetDateTime eventDate;

    @Column(nullable = false)
    private String venue;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Inverse side of the relationship. Lazy + no cascade-delete here on
    // purpose: seat lifecycle is managed explicitly by the service layer,
    // not implicitly via cascading entity graphs.
    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Seat> seats = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
