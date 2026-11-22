import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { api, errorMessage, type Booking } from '../api';
import PaymentModal from '../components/PaymentModal';
import { dayLabel, money, statusLabel, timeLabel } from '../format';
import { useFetch } from '../hooks';

export default function MyBookings() {
  const loc = useLocation();
  const paid = (loc.state as { paid?: string } | null)?.paid;
  const { data, error, loading, reload } = useFetch(() => api<Booking[]>('/bookings/my'), []);
  const [paying, setPaying] = useState<Booking | null>(null);
  const [problem, setProblem] = useState<string | null>(null);

  async function cancel(b: Booking) {
    if (!window.confirm(`Cancel your booking for "${b.classTitle}"?`)) return;
    setProblem(null);
    try {
      await api(`/bookings/${b.id}`, { method: 'DELETE' });
      reload();
    } catch (e) {
      setProblem(errorMessage(e));
    }
  }

  const live = (b: Booking) => b.status === 'PENDING' && b.holdExpiresAt && new Date(b.holdExpiresAt).getTime() > Date.now();

  return (
    <div className="wrap page">
      <h1>My bookings</h1>
      {paid && <p className="notice" role="status">You're booked into {paid}. See you there.</p>}
      {error && <p className="alert" role="alert">{error}</p>}
      {problem && <p className="alert" role="alert">{problem}</p>}
      {loading && !data && <p className="muted">Loading…</p>}

      {data && data.length === 0 && (
        <div className="empty">
          <h2>No bookings yet</h2>
          <p className="muted">Find a session and your seats will show up here.</p>
          <Link className="btn btn-solid" to="/classes">Browse classes</Link>
        </div>
      )}

      <ul className="list">
        {data?.map((b) => (
          <li key={b.id} className="row">
            <div>
              <Link to={`/classes/${b.classId}`}><b>{b.classTitle}</b></Link>
              <p className="muted">{dayLabel(b.startsAt)}, {timeLabel(b.startsAt)}. {b.seats} {b.seats === 1 ? 'seat' : 'seats'}, {money(b.totalCents)}</p>
              <p className="muted small">{b.location}</p>
            </div>
            <span className={`badge badge-${b.status.toLowerCase()}`}>{statusLabel(b.status)}</span>
            <div className="row-actions">
              {live(b) && <button className="btn btn-solid" onClick={() => setPaying(b)}>Pay now</button>}
              {(b.status === 'CONFIRMED' || live(b)) && <button className="btn btn-quiet" onClick={() => cancel(b)}>Cancel</button>}
            </div>
          </li>
        ))}
      </ul>

      {paying && (
        <PaymentModal booking={paying} onClose={() => { setPaying(null); reload(); }}
          onPaid={() => { setPaying(null); reload(); }} />
      )}
    </div>
  );
}
