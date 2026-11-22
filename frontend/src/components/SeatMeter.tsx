/** One tick per seat (capped), filled ticks are taken. The number is the real information; the ticks make it scannable. */
export default function SeatMeter({ capacity, seatsLeft }: { capacity: number; seatsLeft: number }) {
  const ticks = Math.min(capacity, 12);
  const taken = capacity - seatsLeft;
  const filled = Math.round((taken / capacity) * ticks);
  const label = seatsLeft === 0 ? 'Full' : seatsLeft === 1 ? '1 seat left' : `${seatsLeft} seats left`;
  return (
    <div className={`seats ${seatsLeft === 0 ? 'seats-full' : seatsLeft <= 2 ? 'seats-low' : ''}`}>
      <span className="seat-ticks" aria-hidden="true">
        {Array.from({ length: ticks }, (_, i) => <i key={i} className={i < filled ? 'on' : ''} />)}
      </span>
      <span>{label}</span>
    </div>
  );
}
