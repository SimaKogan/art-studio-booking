export const money = (cents: number) =>
  cents === 0 ? 'Free' : `₪${(cents / 100).toLocaleString('en-US', { maximumFractionDigits: 2 })}`;

export const dayLabel = (iso: string) =>
  new Date(iso).toLocaleDateString('en-GB', { weekday: 'short', day: 'numeric', month: 'short' });

export const timeLabel = (iso: string) =>
  new Date(iso).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });

export const fullDate = (iso: string) =>
  new Date(iso).toLocaleDateString('en-GB', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });

export const toInputValue = (iso: string) => iso.slice(0, 16);

export const levelLabel = (l: string) =>
  ({ BEGINNER: 'Beginner', INTERMEDIATE: 'Intermediate', ALL_LEVELS: 'All levels' } as Record<string, string>)[l] ?? l;

export const statusLabel = (s: string) =>
  ({ PENDING: 'Awaiting payment', CONFIRMED: 'Confirmed', CANCELLED: 'Cancelled', EXPIRED: 'Hold expired' } as Record<string, string>)[s] ?? s;
