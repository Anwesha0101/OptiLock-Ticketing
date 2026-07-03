package com.anwesha.optilock_ticketing.service;

import com.anwesha.optilock_ticketing.entity.Booking;
import com.anwesha.optilock_ticketing.entity.Seat;
import com.anwesha.optilock_ticketing.entity.User;

import com.anwesha.optilock_ticketing.enums.BookingStatus;
import com.anwesha.optilock_ticketing.enums.SeatStatus;

import com.anwesha.optilock_ticketing.event.SeatStatusChangedEvent;

import com.anwesha.optilock_ticketing.exception.ResourceNotFoundException;
import com.anwesha.optilock_ticketing.exception.SeatNotAvailableException;

import com.anwesha.optilock_ticketing.repository.BookingRepository;
import com.anwesha.optilock_ticketing.repository.SeatRepository;
import com.anwesha.optilock_ticketing.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * Owns the seat-checkout transaction - the single most important piece
 * of business logic in this system.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Attempts to reserve {@code seatId} on behalf of {@code userId}.
     *
     * <h2>Why this method is safe under concurrent load</h2>
     * Picture two users, A and B, both clicking the same seat within
     * the same millisecond. Both requests reach this method and both
     * threads execute {@code seatRepository.findById(seatId)} - each
     * gets its own in-memory copy of the Seat entity, both snapshotting
     * the SAME version number, say {@code version = 4}.
     * <p>
     * Both threads proceed to flip {@code status} to BOOKED and call
     * {@code save()}. Whichever thread's transaction commits FIRST
     * succeeds normally: Hibernate issues
     * {@code UPDATE seats SET status='BOOKED', version=5 WHERE id=? AND version=4}
     * which matches 1 row, and that thread's Booking row is persisted.
     * <p>
     * The second thread's transaction then tries the exact same
     * conditional UPDATE, but the row's version in the database is now
     * {@code 5}, not {@code 4} - so its
     * {@code WHERE id=? AND version=4} clause matches ZERO rows.
     * Hibernate detects this at flush time and throws
     * {@code ObjectOptimisticLockingFailureException}. We deliberately
     * do NOT catch it here: it propagates up to
     * {@link com.eventbooking.exception.GlobalExceptionHandler}, which
     * turns it into an HTTP 409 Conflict for the losing client. No
     * manual locking (no {@code SELECT ... FOR UPDATE}, no
     * synchronized blocks, no distributed lock) is needed - the
     * database's own row-versioning check does all the work.
     *
     * @param seatId the seat being requested
     * @param userId the user attempting to book it
     * @return the persisted, CONFIRMED Booking
     * @throws ResourceNotFoundException  if the seat or user doesn't exist
     * @throws SeatNotAvailableException  if the seat is already LOCKED/BOOKED
     *                                     (a plain business-rule check, not a race)
     */
    @Transactional
    public Booking bookSeat(Long seatId, Long userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found: id=" + seatId));

        System.out.println("Seat id = " + seat.getId());
        System.out.println("Seat status = " + seat.getStatus());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + userId));

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new SeatNotAvailableException(
                    "Seat " + seat.getSeatNumber() + " is not available (status=" + seat.getStatus() + ")");
        }

        // Flip the in-memory entity's state. The @Version field on Seat
        // is NOT touched here - Hibernate increments it automatically
        // as part of the UPDATE it generates when this transaction
        // flushes/commits.
        seat.setStatus(SeatStatus.BOOKED);
        seatRepository.saveAndFlush(seat);
        // ^ This is the line that can throw ObjectOptimisticLockingFailureException
        //   if another transaction already booked this exact seat first.
        //   (Depending on flush timing it may instead surface when the
        //   @Transactional method returns and the persistence context
        //   flushes - either way it happens inside this transactional
        //   boundary and propagates the same way.)

        Booking booking = Booking.builder()
                .user(user)
                .seat(seat)
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking saved = bookingRepository.saveAndFlush(booking);
        log.info("Seat {} booked by user {} (bookingId={})", seatId, userId, saved.getId());

        // Published now, but NOT delivered to SSE subscribers until this
        // @Transactional method's transaction actually commits - see
        // SeatEventStreamService, which listens with
        // @TransactionalEventListener(phase = AFTER_COMMIT). This
        // guarantees clients are never notified of a seat change that
        // later gets rolled back (e.g. by an unrelated failure further
        // up the call stack).
        eventPublisher.publishEvent(new SeatStatusChangedEvent(
                seat.getEvent().getId(),
                seat.getId(),
                seat.getSeatNumber(),
                SeatStatus.AVAILABLE.name(),
                seat.getStatus().name()
        ));

        return saved;
    }
}
