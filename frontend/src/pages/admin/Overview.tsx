import { api, type Stats } from '../../api';
import { CATEGORIES } from '../../categories';
import { dayLabel, money } from '../../format';
import { useFetch } from '../../hooks';

export default function Overview() {
  const { data: s, error, loading } = useFetch(() => api<Stats>('/admin/stats'), []);
  if (loading && !s) return <p className="muted">Loading numbers…</p>;
  if (error || !s) return <p className="alert" role="alert">{error}</p>;

  const maxRev = Math.max(1, ...s.categories.map((c) => c.revenueCents));
  const upcoming = s.classes.filter((c) => new Date(c.startsAt).getTime() > Date.now());

  return (
    <>
      <dl className="kpis">
        <div><dt>Revenue</dt><dd>{money(s.revenueCents)}</dd></div>
        <div><dt>Confirmed bookings</dt><dd>{s.confirmedBookings}</dd></div>
        <div><dt>Awaiting payment</dt><dd>{s.pendingBookings}</dd></div>
        <div><dt>Average fill of upcoming classes</dt><dd>{Math.round(s.averageOccupancy * 100)}%</dd></div>
        <div><dt>Registered users</dt><dd>{s.users}</dd></div>
      </dl>

      <div className="admin-cols">
        <section>
          <h2>Seats filled, upcoming classes</h2>
          {upcoming.length === 0 && <p className="muted">No upcoming classes. Add one in the Classes tab.</p>}
          <ul className="bars">
            {upcoming.map((c) => {
              const pct = Math.round((c.booked / c.capacity) * 100);
              return (
                <li key={c.classId} style={{ ['--pigment' as string]: CATEGORIES[c.category].color }}>
                  <div className="bar-label"><span>{c.title}</span><span className="muted">{dayLabel(c.startsAt)}</span></div>
                  <div className="bar" role="img" aria-label={`${c.booked} of ${c.capacity} seats`}><i style={{ width: `${pct}%` }} /></div>
                  <span className="bar-num">{c.booked}/{c.capacity}</span>
                </li>
              );
            })}
          </ul>
        </section>

        <section>
          <h2>Revenue by medium</h2>
          {s.categories.length === 0 && <p className="muted">No paid bookings yet.</p>}
          <ul className="bars">
            {s.categories.map((c) => (
              <li key={c.category} style={{ ['--pigment' as string]: CATEGORIES[c.category].color }}>
                <div className="bar-label"><span>{CATEGORIES[c.category].label}</span><span className="muted">{c.bookings} bookings</span></div>
                <div className="bar" role="img" aria-label={money(c.revenueCents)}><i style={{ width: `${(c.revenueCents / maxRev) * 100}%` }} /></div>
                <span className="bar-num">{money(c.revenueCents)}</span>
              </li>
            ))}
          </ul>
        </section>
      </div>
    </>
  );
}
