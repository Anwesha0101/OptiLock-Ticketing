import Seat from './Seat';

/**
 * Groups seats by the leading alphabetic prefix of their seat number
 * (e.g. "A1", "A2" -> row "A"). Falls back to a single row if the
 * naming scheme doesn't match that pattern.
 */
function groupIntoRows(seats) {
  const rows = new Map();
  for (const seat of seats) {
    const match = seat.seatNumber.match(/^([A-Za-z]+)/);
    const rowKey = match ? match[1].toUpperCase() : 'SEATS';
    if (!rows.has(rowKey)) rows.set(rowKey, []);
    rows.get(rowKey).push(seat);
  }
  return [...rows.entries()].sort(([a], [b]) => a.localeCompare(b));
}

export default function SeatGrid({ seats, onSelect, pendingSeatId }) {
  const rows = groupIntoRows(seats);

  return (
    <div>
      <div className="mb-10 flex flex-col items-center">
        <div className="w-full max-w-md h-2 rounded-full bg-gradient-to-r from-transparent via-marquee/70 to-transparent" />
        <p className="mt-2 font-mono text-[10px] tracking-[0.3em] text-paper-faint uppercase">
          Stage
        </p>
      </div>

      <div className="space-y-2.5 flex flex-col items-center">
        {rows.map(([rowKey, rowSeats]) => (
          <div key={rowKey} className="flex items-center gap-2.5">
            <span className="w-5 font-mono text-xs text-paper-faint text-right">
              {rowKey}
            </span>
            <div className="flex gap-2 flex-wrap justify-center">
              {rowSeats.map((seat) => (
                <Seat
                  key={seat.id}
                  seat={seat}
                  onSelect={onSelect}
                  isPending={seat.id === pendingSeatId}
                />
              ))}
            </div>
          </div>
        ))}
      </div>

      <div className="mt-10 flex items-center justify-center gap-6 font-mono text-xs text-paper-dim">
        <Legend swatch="bg-marquee" label="Available" />
        <Legend swatch="bg-slate-seat" label="Locked" />
        <Legend swatch="bg-velvet" label="Booked" />
      </div>
    </div>
  );
}

function Legend({ swatch, label }) {
  return (
    <span className="flex items-center gap-2">
      <span className={`w-3 h-3 rounded-sm ${swatch}`} />
      {label}
    </span>
  );
}
