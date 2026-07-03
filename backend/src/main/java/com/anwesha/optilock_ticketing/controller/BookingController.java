package com.anwesha.optilock_ticketing.controller;

import com.anwesha.optilock_ticketing.dto.BookingDtos.BookingRequest;
import com.anwesha.optilock_ticketing.dto.BookingDtos.BookingResponse;
import com.anwesha.optilock_ticketing.entity.Booking;
import com.anwesha.optilock_ticketing.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
/**
 * All endpoints here are protected by SecurityConfig
 * ("/api/bookings/** -> authenticated()"). The JwtAuthenticationFilter
 * places the authenticated user's id as the Authentication principal,
 * so we read it straight off the security context rather than trusting
 * a userId supplied by the client in the request body - a client can
 * only ever book seats as themselves.
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> bookSeat(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        Booking booking = bookingService.bookSeat(request.seatId(), userId);
        // If two clients race for the same seat, the loser never reaches
        // this line - BookingService.bookSeat throws
        // ObjectOptimisticLockingFailureException, which
        // GlobalExceptionHandler turns into a 409 before we get here.

        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }
}
