package com.anwesha.optilock_ticketing.repository;

import com.anwesha.optilock_ticketing.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    /**
     * Powers the seat-grid endpoint (Phase 4): all seats for an event,
     * ordered so the frontend can render a stable grid layout.
     */
    List<Seat> findByEventIdOrderBySeatNumberAsc(Long eventId);

    /**
     * Plain findById is intentionally what the booking service uses too
     * (see BookingService.bookSeat). We deliberately do NOT use
     * @Lock(LockModeType.PESSIMISTIC_WRITE) here - the whole point of
     * this project is to demonstrate optimistic locking via the
     * entity's @Version field instead of taking a DB row lock.
     */
}
