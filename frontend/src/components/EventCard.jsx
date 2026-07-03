import { Link } from 'react-router-dom';

const dateFormatter = new Intl.DateTimeFormat('en-US', {
  weekday: 'short',
  month: 'short',
  day: 'numeric',
  hour: 'numeric',
  minute: '2-digit',
});

export default function EventCard({ event }) {
  return (
    <Link
      to={`/events/${event.id}`}
      className="group block rounded-lg border border-ink-line bg-ink-soft p-6 hover:border-marquee/60 hover:shadow-glow transition-all"
    >
      <p className="font-mono text-xs uppercase tracking-wide text-marquee mb-3">
        {dateFormatter.format(new Date(event.eventDate))}
      </p>
      <h2 className="font-display text-3xl tracking-wide leading-none mb-2 group-hover:text-marquee transition-colors">
        {event.name}
      </h2>
      <p className="text-paper-dim text-sm mb-4">{event.venue}</p>
      <p className="font-mono text-xs text-paper-faint">
        {event.totalSeats} seats total
      </p>
    </Link>
  );
}
