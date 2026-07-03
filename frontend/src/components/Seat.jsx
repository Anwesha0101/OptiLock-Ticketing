const STATUS_STYLES = {
  AVAILABLE:
    'bg-marquee/15 border-marquee text-marquee hover:bg-marquee hover:text-ink cursor-pointer',
  LOCKED:
    'bg-slate-seat/20 border-slate-seat text-slate-seat cursor-not-allowed',
  BOOKED:
    'bg-velvet/20 border-velvet text-velvet-bright cursor-not-allowed',
};

export default function Seat({ seat, onSelect, isPending }) {
  const clickable = seat.status === 'AVAILABLE' && !isPending;

  return (
    <button
      type="button"
      disabled={!clickable}
      onClick={() => clickable && onSelect(seat)}
      title={`Seat ${seat.seatNumber} — ₹${Number(seat.price).toFixed(2)} — ${seat.status}`}
      className={`seat-stub w-9 h-9 border text-[10px] font-mono flex items-center justify-center transition-colors ${STATUS_STYLES[seat.status]} ${
        isPending ? 'animate-pulse opacity-60' : ''
      }`}
    >
      {seat.seatNumber}
    </button>
  );
}
