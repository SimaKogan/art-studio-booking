import { useState } from 'react';
import AdminBookings from './AdminBookings';
import AdminClasses from './AdminClasses';
import Overview from './Overview';

const TABS = [
  { id: 'overview', label: 'Overview' },
  { id: 'classes', label: 'Classes' },
  { id: 'bookings', label: 'Bookings' },
] as const;

export default function Admin() {
  const [tab, setTab] = useState<(typeof TABS)[number]['id']>('overview');
  return (
    <div className="wrap page">
      <h1>Studio admin</h1>
      <div className="tabs" role="tablist">
        {TABS.map((t) => (
          <button key={t.id} role="tab" aria-selected={tab === t.id} className={tab === t.id ? 'tab-on' : ''} onClick={() => setTab(t.id)}>
            {t.label}
          </button>
        ))}
      </div>
      {tab === 'overview' && <Overview />}
      {tab === 'classes' && <AdminClasses />}
      {tab === 'bookings' && <AdminBookings />}
    </div>
  );
}
