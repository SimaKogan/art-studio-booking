import { useEffect, useRef, useState, type FormEvent } from 'react';
import { api, errorMessage, type Booking, type Payment } from '../api';
import { money } from '../format';

interface Props {
  booking: Booking;
  onClose: () => void;
  onPaid: (p: Payment) => void;
}

const groupCard = (v: string) => v.replace(/\D/g, '').slice(0, 19).replace(/(.{4})/g, '$1 ').trim();
const formatExpiry = (v: string) => {
  const d = v.replace(/\D/g, '').slice(0, 4);
  return d.length > 2 ? `${d.slice(0, 2)}/${d.slice(2)}` : d;
};

/** Checkout against the payment stub. Shows the seat-hold countdown and the test cards. */
export default function PaymentModal({ booking, onClose, onPaid }: Props) {
  const [form, setForm] = useState({ cardholder: '', cardNumber: '', expiry: '', cvc: '' });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [now, setNow] = useState(Date.now());
  const first = useRef<HTMLInputElement>(null);
  const closeRef = useRef(onClose);
  closeRef.current = onClose;

  // Runs once: parents pass inline callbacks, so depending on them would re-steal focus on every render.
  useEffect(() => {
    first.current?.focus();
    const t = setInterval(() => setNow(Date.now()), 1000);
    const esc = (e: KeyboardEvent) => { if (e.key === 'Escape') closeRef.current(); };
    window.addEventListener('keydown', esc);
    return () => { clearInterval(t); window.removeEventListener('keydown', esc); };
  }, []);

  const left = booking.holdExpiresAt ? Math.max(0, Math.floor((new Date(booking.holdExpiresAt).getTime() - now) / 1000)) : 0;
  const mm = String(Math.floor(left / 60)).padStart(2, '0');
  const ss = String(left % 60).padStart(2, '0');

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const p = await api<Payment>('/payments/checkout', { method: 'POST', body: { bookingId: booking.id, ...form } });
      if (p.status === 'SUCCEEDED') onPaid(p);
      else setError(p.message);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  const set = (k: keyof typeof form) => (v: string) => setForm((f) => ({ ...f, [k]: v }));

  return (
    <div className="modal-back" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <form className="modal" onSubmit={submit} role="dialog" aria-modal="true" aria-labelledby="pay-title">
        <div className="modal-head">
          <div>
            <h2 id="pay-title">Pay {money(booking.totalCents)}</h2>
            <p className="muted">{booking.classTitle}, {booking.seats} {booking.seats === 1 ? 'seat' : 'seats'}</p>
          </div>
          <button type="button" className="btn btn-quiet" onClick={onClose} aria-label="Close">Close</button>
        </div>

        <p className={`hold ${left === 0 ? 'hold-out' : ''}`} role="timer">
          {left > 0 ? <>Your seats are held for <b>{mm}:{ss}</b></> : 'Your hold has expired. Close this and book again.'}
        </p>

        <label>Name on card
          <input ref={first} required value={form.cardholder} onChange={(e) => set('cardholder')(e.target.value)} autoComplete="cc-name" />
        </label>
        <label>Card number
          <input required inputMode="numeric" placeholder="4242 4242 4242 4242" value={form.cardNumber}
            onChange={(e) => set('cardNumber')(groupCard(e.target.value))} autoComplete="cc-number" />
        </label>
        <div className="row2">
          <label>Expiry
            <input required inputMode="numeric" placeholder="MM/YY" value={form.expiry}
              onChange={(e) => set('expiry')(formatExpiry(e.target.value))} autoComplete="cc-exp" />
          </label>
          <label>CVC
            <input required inputMode="numeric" maxLength={4} placeholder="123" value={form.cvc}
              onChange={(e) => set('cvc')(e.target.value.replace(/\D/g, ''))} autoComplete="cc-csc" />
          </label>
        </div>

        {error && <p className="alert" role="alert">{error}</p>}

        <div className="testcards">
          <span>Stub checkout. Try a test card:</span>
          <button type="button" onClick={() => setForm((f) => ({ ...f, cardholder: f.cardholder || 'Demo Student', cardNumber: '4242 4242 4242 4242', expiry: '12/40', cvc: '123' }))}>
            Approved
          </button>
          <button type="button" onClick={() => setForm((f) => ({ ...f, cardholder: f.cardholder || 'Demo Student', cardNumber: '4000 0000 0000 0002', expiry: '12/40', cvc: '123' }))}>
            Declined
          </button>
        </div>

        <button className="btn btn-solid btn-wide" disabled={busy || left === 0}>
          {busy ? 'Processing…' : `Pay ${money(booking.totalCents)}`}
        </button>
      </form>
    </div>
  );
}
