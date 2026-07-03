package com.anwesha.optilock_ticketing.controller;

import com.anwesha.optilock_ticketing.dto.EventDtos.EventDetailDto;
import com.anwesha.optilock_ticketing.dto.EventDtos.EventSummaryDto;
import com.anwesha.optilock_ticketing.dto.EventDtos.SeatDto;

import com.anwesha.optilock_ticketing.entity.Event;

import com.anwesha.optilock_ticketing.exception.ResourceNotFoundException;

import com.anwesha.optilock_ticketing.repository.EventRepository;
import com.anwesha.optilock_ticketing.repository.SeatRepository;

import com.anwesha.optilock_ticketing.sse.SeatEventStreamService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final SeatEventStreamService seatEventStreamService;

    /** Public: dashboard list of all events. */
    @GetMapping
    public List<EventSummaryDto> listEvents() {
        return eventRepository.findAllByOrderByEventDateAsc().stream()
                .map(EventSummaryDto::from)
                .toList();
    }

    /** Public: single event + its full seat grid. */
    @GetMapping("/{id}")
    public EventDetailDto getEvent(@PathVariable Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: id=" + id));

        List<SeatDto> seats = seatRepository.findByEventIdOrderBySeatNumberAsc(id).stream()
                .map(SeatDto::from)
                .toList();

        return new EventDetailDto(
                event.getId(), event.getName(), event.getEventDate(),
                event.getVenue(), event.getTotalSeats(), seats);
    }

    /** Public: seat grid alone (used for a lightweight polling fallback / refresh). */
    @GetMapping("/{id}/seats")
    public List<SeatDto> getSeats(@PathVariable Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found: id=" + id);
        }
        return seatRepository.findByEventIdOrderBySeatNumberAsc(id).stream()
                .map(SeatDto::from)
                .toList();
    }

    /**
     * Public SSE stream: clients subscribe here to receive
     * "seat-status-changed" events in real time as bookings commit.
     * MediaType.TEXT_EVENT_STREAM_VALUE ensures Spring negotiates the
     * response as text/event-stream rather than JSON.
     */
    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSeatUpdates(@PathVariable Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found: id=" + id);
        }
        return seatEventStreamService.subscribe(id);
    }
}
