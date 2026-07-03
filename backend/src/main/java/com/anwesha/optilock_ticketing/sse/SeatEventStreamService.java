package com.anwesha.optilock_ticketing.sse;

import com.anwesha.optilock_ticketing.event.SeatStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory registry of live SSE connections, keyed by event id, plus
 * the transactional listener that pushes updates out to them.
 *
 * NOTE: this in-memory approach works great for a single application
 * instance. If you horizontally scale this service across multiple
 * nodes, you'd need to fan the {@link SeatStatusChangedEvent} out
 * through a shared broker (Redis Pub/Sub, Kafka, etc.) so every node's
 * local emitters get notified, not just the node that handled the
 * booking request.
 */
@Slf4j
@Service
public class SeatEventStreamService {

    private static final long EMITTER_TIMEOUT_MS = 30 * 60 * 1000L; // 30 minutes

    private final Map<Long, List<SseEmitter>> emittersByEventId = new ConcurrentHashMap<>();

    /**
     * Called by the controller when a client opens
     * GET /api/events/{id}/stream. Registers a new emitter and wires up
     * cleanup callbacks so we never leak dead connections.
     */
    public SseEmitter subscribe(Long eventId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        List<SseEmitter> emitters = emittersByEventId.computeIfAbsent(eventId, id -> new CopyOnWriteArrayList<>());
        emitters.add(emitter);

        emitter.onCompletion(() -> removeEmitter(eventId, emitter));
        emitter.onTimeout(() -> removeEmitter(eventId, emitter));
        emitter.onError(ex -> removeEmitter(eventId, emitter));

        // Send an initial comment/ping so proxies don't buffer the
        // connection and the client knows the stream is live.
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("subscribed to event " + eventId));
        } catch (IOException ex) {
            removeEmitter(eventId, emitter);
        }

        return emitter;
    }

    private void removeEmitter(Long eventId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByEventId.get(eventId);
        if (emitters != null) {
            emitters.remove(emitter);
        }
    }

    /**
     * Fires only AFTER the enclosing booking transaction has committed
     * successfully - this is the key guarantee that keeps every
     * connected client's seat grid consistent with what's actually in
     * the database. If the transaction rolled back (e.g. the
     * optimistic-lock exception case), this listener never runs at all.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSeatStatusChanged(SeatStatusChangedEvent event) {
        List<SseEmitter> emitters = emittersByEventId.get(event.eventId());
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        log.debug("Broadcasting seat {} status {} -> {} to {} subscriber(s)",
                event.seatId(), event.previousStatus(), event.newStatus(), emitters.size());

        for (SseEmitter emitter : List.copyOf(emitters)) {
            try {
                emitter.send(SseEmitter.event()
                        .name("seat-status-changed")
                        .data(event));
            } catch (IOException ex) {
                // Client disconnected without a clean close - drop it.
                removeEmitter(event.eventId(), emitter);
            }
        }
    }
}
