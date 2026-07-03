package com.anwesha.optilock_ticketing.service;

import com.anwesha.optilock_ticketing.entity.Event;
import com.anwesha.optilock_ticketing.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read-side service for browsing events. Kept intentionally thin -
 * event creation/administration is out of scope here.
 */
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    /**
     * Returns all events ordered by event date, soonest first.
     */
    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        return eventRepository.findAllByOrderByEventDateAsc();
    }
}
