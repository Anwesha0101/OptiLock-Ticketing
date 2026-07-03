package com.anwesha.optilock_ticketing.repository;

import com.anwesha.optilock_ticketing.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    Optional<Booking> findBySeatId(Long seatId);
}
