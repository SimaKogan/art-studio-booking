import { useState } from 'react';
import { api, errorMessage, type Booking } from '../../api';
import { dayLabel, money, statusLabel, timeLabel } from '../../format';
import { useFetch } from '../../hooks';

export default function AdminBookings() {
  const { data, error, loading, reload } = useFetch(() => api<Booking[]>('/admin/bookings'), []);
  const [problem, setProblem] = useState<string | null>(null);

  async function cancel(b: Booking) {
    if (!window.confirm(`Cancel ${b.userName}'s booking for "${b.classTitle}"? Paid bookings are refunded.`)) return;
    setProblem(null);
    try { await api(`/admin/bookings/${b.id}`, { method: 'DELETE' }); reload(); }
    catch (e) { setProblem(errorMessage(e)); }
  }

  if (loading && !data) return <p className="muted">Loading…</p>;
  return (
    <>
      {(error || problem) && <p className="alert" role="alert">{error ?? problem}</p>}
      {data && data.length === 0 && <p className="muted">No bookings yet.</p>}
      <div className="table-wrap">
        <table>
          <thead><tr><th>Student</th><th>Class</th><th>Date</th><th>Seats</th><th>Total</th><th>Status</th><th><span className="sr">Actions</span></th></tr></thead>
          <tbody>
            {data?.map((b) => (
              <tr key={b.id}>
                <td>{b.userName}<br /><span className="muted small">{b.userEmail}</span></td>
                <td>{b.classTitle}</td>
                <td>{dayLabel(b.startsAt)}, {timeLabel(b.startsAt)}</td>
                <td>{b.seats}</td>
                <td>{money(b.totalCents)}</td>
                <td><span className={`badge badge-${b.status.toLowerCase()}`}>{statusLabel(b.status)}</span></td>
                <td>{(b.status === 'CONFIRMED' || b.status === 'PENDING') && <button className="btn btn-quiet" onClick={() => cancel(b)}>Cancel</button>}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
