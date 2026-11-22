import { useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { api, errorMessage, type Booking, type StudioClass } from '../api';
import { useAuth } from '../auth';
import { CATEGORIES } from '../categories';
import CoverArt from '../components/CoverArt';
import PaymentModal from '../components/PaymentModal';
import SeatMeter from '../components/SeatMeter';
import { fullDate, levelLabel, money, timeLabel } from '../format';
import { useFetch } from '../hooks';

export default function ClassDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const nav = useNavigate();
  const loc = useLocation();
  const { data: c, error, loading, reload } = useFetch(() => api<StudioClass>(`/classes/${id}`), [id]);
  const [seats, setSeats] = useState(1);
  const [busy, setBusy] = useState(false);
  const [problem, setProblem] = useState<string | null>(null);
  const [booking, setBooking] = useState<Booking | null>(null);

  if (loading && !c) return <div className="wrap page"><p className="muted">Loading…</p></div>;
  if (error || !c) {
    return (
      <div className="wrap page">
        <h1>We couldn't find that class</h1>
        <p className="alert">{error}</p>
        <Link className="btn btn-line" to="/classes">Back to classes</Link>
      </div>
    );
  }

  const cat = CATEGORIES[c.category];
  const maxSeats = Math.max(1, Math.min(6, c.seatsLeft));
  const past = new Date(c.startsAt).getTime() < Date.now();
  const closed = !c.active || past || c.seatsLeft === 0;

  async function book() {
    if (!c) return;
    if (!user) { nav('/login', { state: { from: loc.pathname } }); return; }
    setBusy(true);
    setProblem(null);
    try {
      setBooking(await api<Booking>('/bookings', { method: 'POST', body: { classId: c.id, seats } }));
    } catch (e) {
      setProblem(errorMessage(e));
      reload();
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="wrap page">
      <Link to="/classes" className="back">All classes</Link>
      <div className="detail" style={{ ['--pigment' as string]: cat.color, ['--on-pigment' as string]: cat.ink }}>
        <article>
          <div className="detail-cover"><CoverArt category={c.category} /><span className="card-cat">{cat.label}</span></div>
          <h1>{c.title}</h1>
          <p className="lede-sm">{c.description}</p>
          <dl className="facts">
            <div><dt>When</dt><dd>{fullDate(c.startsAt)}, {timeLabel(c.startsAt)}</dd></div>
            <div><dt>Length</dt><dd>{Math.floor(c.durationMinutes / 60) > 0 ? `${Math.floor(c.durationMinutes / 60)} h ` : ''}{c.durationMinutes % 60 ? `${c.durationMinutes % 60} min` : ''}</dd></div>
            <div><dt>Level</dt><dd>{levelLabel(c.level)}</dd></div>
            <div><dt>Teacher</dt><dd>{c.instructor}</dd></div>
            <div><dt>Where</dt><dd>{c.location}</dd></div>
          </dl>
        </article>

        <aside className="book">
          <p className="book-price">{money(c.priceCents)} <span>per seat</span></p>
          <SeatMeter capacity={c.capacity} seatsLeft={c.seatsLeft} />

          {!closed && (
            <>
              <label>Seats
                <div className="stepper">
                  <button type="button" onClick={() => setSeats((s) => Math.max(1, s - 1))} aria-label="One seat fewer" disabled={seats <= 1}>−</button>
                  <output aria-live="polite">{Math.min(seats, maxSeats)}</output>
                  <button type="button" onClick={() => setSeats((s) => Math.min(maxSeats, s + 1))} aria-label="One seat more" disabled={seats >= maxSeats}>+</button>
                </div>
              </label>
              <p className="book-total">Total <b>{money(c.priceCents * Math.min(seats, maxSeats))}</b></p>
            </>
          )}

          {problem && <p className="alert" role="alert">{problem}</p>}
          <button className="btn btn-solid btn-wide" disabled={busy || closed} onClick={book}>
            {closed ? (c.seatsLeft === 0 ? 'Class is full' : 'Not open for booking') : busy ? 'Holding your seats…' : user ? 'Book and pay' : 'Log in to book'}
          </button>
          {!closed && <p className="muted small">Seats are held for 15 minutes while you check out. Free cancellation until 24 hours before.</p>}
        </aside>
      </div>

      {booking && (
        <PaymentModal booking={booking}
          onClose={() => { setBooking(null); reload(); }}
          onPaid={() => nav('/bookings', { state: { paid: c.title } })} />
      )}
    </div>
  );
}
