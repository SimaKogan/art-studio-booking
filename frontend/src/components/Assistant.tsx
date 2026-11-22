import { useEffect, useRef, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { api, errorMessage, type ChatResponse, type ChatTurn, type StudioClass } from '../api';
import { CATEGORIES } from '../categories';
import { dayLabel, money, timeLabel } from '../format';

interface Msg extends ChatTurn { suggestions?: StudioClass[] }

const PROMPTS = [
  'Beginner watercolor this week',
  'Something under ₪150',
  'Ceramics on the weekend',
  'A class for my kid',
];

/** Floating chat: asks the backend assistant (retrieval + LLM) and shows the matched classes as links. */
export default function Assistant() {
  const [open, setOpen] = useState(false);
  const [msgs, setMsgs] = useState<Msg[]>([]);
  const [text, setText] = useState('');
  const [busy, setBusy] = useState(false);
  const [provider, setProvider] = useState<string | null>(null);
  const end = useRef<HTMLDivElement>(null);

  useEffect(() => { end.current?.scrollIntoView({ block: 'end' }); }, [msgs, busy, open]);

  async function send(message: string) {
    const q = message.trim();
    if (!q || busy) return;
    const history: ChatTurn[] = msgs.slice(-8).map(({ role, text }) => ({ role, text }));
    setMsgs((m) => [...m, { role: 'user', text: q }]);
    setText('');
    setBusy(true);
    try {
      const r = await api<ChatResponse>('/assistant/chat', { method: 'POST', body: { message: q, history } });
      setProvider(r.provider);
      setMsgs((m) => [...m, { role: 'assistant', text: r.reply, suggestions: r.suggestions }]);
    } catch (e) {
      setMsgs((m) => [...m, { role: 'assistant', text: errorMessage(e) }]);
    } finally {
      setBusy(false);
    }
  }

  const submit = (e: FormEvent) => { e.preventDefault(); void send(text); };

  return (
    <>
      <button className="ask-fab" onClick={() => setOpen((o) => !o)} aria-expanded={open} aria-controls="ask-panel">
        {open ? 'Close assistant' : 'Ask about the schedule'}
      </button>
      {open && (
        <section className="ask" id="ask-panel" aria-label="Schedule assistant">
          <div className="ask-log" aria-live="polite">
            {msgs.length === 0 && (
              <div className="ask-intro">
                <p>Tell me what you'd like to make and when you're free. I'll look through the real schedule.</p>
                <div className="chips">
                  {PROMPTS.map((p) => <button key={p} onClick={() => void send(p)}>{p}</button>)}
                </div>
              </div>
            )}
            {msgs.map((m, i) => (
              <div key={i} className={`bubble bubble-${m.role}`}>
                <p>{m.text}</p>
                {m.suggestions && m.suggestions.length > 0 && (
                  <ul className="picks">
                    {m.suggestions.map((s) => (
                      <li key={s.id}>
                        <Link to={`/classes/${s.id}`} onClick={() => setOpen(false)} style={{ ['--pigment' as string]: CATEGORIES[s.category].color }}>
                          <b>{s.title}</b>
                          <span>{dayLabel(s.startsAt)}, {timeLabel(s.startsAt)}, {money(s.priceCents)}</span>
                        </Link>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            ))}
            {busy && <div className="bubble bubble-assistant"><p className="muted">Checking the schedule…</p></div>}
            <div ref={end} />
          </div>
          <form className="ask-form" onSubmit={submit}>
            <input value={text} onChange={(e) => setText(e.target.value)} maxLength={500} placeholder="e.g. a calm evening class under ₪200" aria-label="Your question" />
            <button className="btn btn-solid" disabled={busy || !text.trim()}>Send</button>
          </form>
          {provider && (
            <p className="ask-note">
              {provider.startsWith('vertex') ? `Answered by ${provider.replace('vertex:', '')} on Vertex AI` : 'Offline demo mode: answers are built from the schedule without an LLM'}
            </p>
          )}
        </section>
      )}
    </>
  );
}
