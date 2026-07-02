package com.anwesha.optilock_ticketing.entity;

//import com.anwesha.optilockticketing.entity.enums.SeatStatus;

import com.anwesha.optilock_ticketing.enums.Role;
import com.anwesha.optilock_ticketing.enums.SeatStatus;
import com.anwesha.optilock_ticketing.enums.BookingStatus;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * A single seat belonging to an {@link Event}.
 *
 * <h2>How Optimistic Locking works here</h2>
 * The {@code version} field below is annotated with {@code @Version}.
 * Hibernate silently manages this column for us:
 * <ol>
 *   <li>When a Seat row is loaded, Hibernate remembers its current
 *       {@code version} value (e.g. 4) as part of the persistence
 *       context / entity snapshot.</li>
 *   <li>When the entity is later flushed (on transaction commit, or
 *       an explicit {@code save()} inside a {@code @Transactional}
 *       method), Hibernate does NOT issue a plain
 *       {@code UPDATE seats SET status = ? WHERE id = ?}.
 *       Instead it issues:
 *       <pre>
 *       UPDATE seats
 *          SET status = ?, version = 5
 *        WHERE id = ? AND version = 4
 *       </pre>
 *   </li>
 *   <li>If another transaction already booked this exact seat in the
 *       meantime, that transaction has already bumped the row to
 *       {@code version = 5}. Our {@code WHERE ... AND version = 4}
 *       clause then matches ZERO rows.</li>
 *   <li>Hibernate detects the affected-row-count mismatch and throws
 *       {@code jakarta.persistence.OptimisticLockException}, which
 *       Spring Data wraps as
 *       {@code org.springframework.orm.ObjectOptimisticLockingFailureException}.</li>
 *   <li>The Phase 3 {@code @RestControllerAdvice} catches that
 *       exception and returns an HTTP 409 Conflict, so the losing
 *       client is told cleanly "someone else grabbed this seat"
 *       instead of silently overwriting the winner's booking.</li>
 * </ol>
 * No manual locking, {@code SELECT ... FOR UPDATE}, or distributed
 * lock service is required - the version column gives us safe,
 * lock-free concurrency control entirely inside the relational engine.
 */
@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(name = "uq_seat_event_seatnumber", columnNames = {"event_id", "seat_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * CRITICAL: this field powers optimistic locking for every write to
     * this entity. Never set it manually - Hibernate owns it entirely.
     */
    @Version
    @Column(nullable = false)
    private Integer version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

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
