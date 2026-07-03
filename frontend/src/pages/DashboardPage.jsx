import { useEffect, useState } from 'react';
import { api } from '../api/client';
import EventCard from '../components/EventCard';

export default function DashboardPage() {
  const [events, setEvents] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    api
      .listEvents()
      .then(setEvents)
      .catch((err) => setError(err.message));
  }, []);

  return (
    <div className="max-w-6xl mx-auto px-6 py-12">
      <p className="font-mono text-xs uppercase tracking-wide text-marquee mb-2">
        Now booking
      </p>
      <h1 className="font-display text-5xl tracking-marquee mb-10">
        UPCOMING EVENTS
      </h1>

      {error && (
        <p className="text-velvet-bright font-mono text-sm">{error}</p>
      )}

      {!events && !error && (
        <p className="text-paper-faint font-mono text-sm">Loading events…</p>
      )}

      {events && events.length === 0 && (
        <p className="text-paper-faint">
          No events on the calendar yet. Check back soon.
        </p>
      )}

      {events && events.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {events.map((event) => (
            <EventCard key={event.id} event={event} />
          ))}
        </div>
      )}
    </div>
  );
}
