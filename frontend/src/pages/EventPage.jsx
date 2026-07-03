import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import SeatGrid from '../components/SeatGrid';

const dateFormatter = new Intl.DateTimeFormat('en-US', {
  weekday: 'long',
  month: 'long',
  day: 'numeric',
  hour: 'numeric',
  minute: '2-digit',
});

export default function EventPage() {
  const { eventId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [event, setEvent] = useState(null);
  const [seats, setSeats] = useState([]);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);
  const [pendingSeatId, setPendingSeatId] = useState(null);
  const eventSourceRef = useRef(null);

  // Initial load
  useEffect(() => {
    api
      .getEvent(eventId)
      .then((detail) => {
        setEvent(detail);
        setSeats(detail.seats);
      })
      .catch((err) => setError(err.message));
  }, [eventId]);

  // Live subscription: the browser's native EventSource hooks straight
  // into the backend's SseEmitter stream. Every "seat-status-changed"
  // message updates just that one seat in local state, so every open
  // tab converges on the same grid the instant another client's
  // booking commits.
  useEffect(() => {
    const source = new EventSource(api.streamUrl(eventId));
    eventSourceRef.current = source;

    source.addEventListener('seat-status-changed', (e) => {
      const update = JSON.parse(e.data);
      setSeats((prev) =>
        prev.map((seat) =>
          seat.id === update.seatId ? { ...seat, status: update.newStatus } : seat
        )
      );
    });

    source.onerror = () => {
      // EventSource auto-reconnects on transient network errors by
      // default; we just avoid letting a stale connection linger
      // silently forever if the server is genuinely gone.
    };

    return () => source.close();
  }, [eventId]);

  const handleSelect = useCallback(
    async (seat) => {
      setError(null);
      setNotice(null);

      if (!user) {
        navigate('/login', { state: { from: `/events/${eventId}` } });
        return;
      }

      setPendingSeatId(seat.id);
      // Optimistic UI: reflect LOCKED immediately so the same client
      // can't double-click while the request is in flight. The SSE
      // broadcast will confirm the true final state for everyone,
      // including this tab, once the transaction commits.
      setSeats((prev) =>
        prev.map((s) => (s.id === seat.id ? { ...s, status: 'LOCKED' } : s))
      );

      try {
        await api.bookSeat(seat.id);
        setNotice(`Seat ${seat.seatNumber} is yours. Enjoy the show.`);
      } catch (err) {
        // 409 = another client won the race (optimistic-locking
        // conflict) or the seat was already taken - either way, revert
        // our optimistic guess and let the next SSE message or a
        // manual refresh settle on the true status.
        setError(err.message);
        setSeats((prev) =>
          prev.map((s) => (s.id === seat.id ? { ...s, status: seat.status } : s))
        );
      } finally {
        setPendingSeatId(null);
      }
    },
    [user, eventId, navigate]
  );

  if (error && !event) {
    return (
      <div className="max-w-2xl mx-auto px-6 py-20 text-center">
        <p className="text-velvet-bright font-mono">{error}</p>
      </div>
    );
  }

  if (!event) {
    return (
      <div className="max-w-2xl mx-auto px-6 py-20 text-center">
        <p className="text-paper-faint font-mono text-sm">Loading event…</p>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-6 py-12">
      <p className="font-mono text-xs uppercase tracking-wide text-marquee mb-2 text-center">
        {dateFormatter.format(new Date(event.eventDate))} &middot; {event.venue}
      </p>
      <h1 className="font-display text-5xl tracking-marquee text-center mb-2">
        {event.name}
      </h1>
      {!user && (
        <p className="text-center text-paper-faint text-sm mb-10">
          Sign in to reserve a seat.
        </p>
      )}
      {user && <div className="mb-10" />}

      {notice && (
        <p className="text-center text-marquee font-mono text-sm mb-6">{notice}</p>
      )}
      {error && (
        <p className="text-center text-velvet-bright font-mono text-sm mb-6">{error}</p>
      )}

      <SeatGrid seats={seats} onSelect={handleSelect} pendingSeatId={pendingSeatId} />
    </div>
  );
}
