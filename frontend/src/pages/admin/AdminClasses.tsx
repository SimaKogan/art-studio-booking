import { useState, type FormEvent } from 'react';
import { api, errorMessage, type Category, type ClassInput, type Level, type StudioClass } from '../../api';
import { CATEGORIES, CATEGORY_ORDER } from '../../categories';
import { dayLabel, levelLabel, money, timeLabel, toInputValue } from '../../format';
import { useFetch } from '../../hooks';

const blank = (): ClassInput => {
  const d = new Date(Date.now() + 7 * 864e5);
  d.setHours(18, 0, 0, 0);
  const local = new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
  return {
    title: '', description: '', category: 'PAINTING', level: 'ALL_LEVELS', instructor: '',
    location: 'Atelier Nahal, 7 Indigo Lane, Rivertown', startsAt: local, durationMinutes: 120,
    priceCents: 18000, capacity: 10, active: true,
  };
};

export default function AdminClasses() {
  const { data, error, loading, reload } = useFetch(() => api<StudioClass[]>('/admin/classes'), []);
  const [editing, setEditing] = useState<{ id: number | null; form: ClassInput } | null>(null);
  const [problem, setProblem] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const edit = (c: StudioClass) => setEditing({
    id: c.id,
    form: { title: c.title, description: c.description, category: c.category, level: c.level, instructor: c.instructor,
      location: c.location, startsAt: toInputValue(c.startsAt), durationMinutes: c.durationMinutes,
      priceCents: c.priceCents, capacity: c.capacity, active: c.active },
  });

  async function save(e: FormEvent) {
    e.preventDefault();
    if (!editing) return;
    setBusy(true);
    setProblem(null);
    try {
      const body = { ...editing.form, startsAt: editing.form.startsAt.length === 16 ? `${editing.form.startsAt}:00` : editing.form.startsAt };
      if (editing.id) await api(`/admin/classes/${editing.id}`, { method: 'PUT', body });
      else await api('/admin/classes', { method: 'POST', body });
      setEditing(null);
      reload();
    } catch (err) {
      setProblem(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  async function hide(c: StudioClass) {
    if (!window.confirm(`Hide "${c.title}" from the public schedule? Existing bookings are kept.`)) return;
    try { await api(`/admin/classes/${c.id}`, { method: 'DELETE' }); reload(); }
    catch (err) { setProblem(errorMessage(err)); }
  }

  const f = editing?.form;
  const set = <K extends keyof ClassInput>(k: K, v: ClassInput[K]) =>
    setEditing((s) => (s ? { ...s, form: { ...s.form, [k]: v } } : s));

  return (
    <>
      <div className="section-head">
        <h2>All classes</h2>
        <button className="btn btn-solid" onClick={() => setEditing({ id: null, form: blank() })}>Add a class</button>
      </div>
      {(error || problem) && !editing && <p className="alert" role="alert">{error ?? problem}</p>}
      {loading && !data && <p className="muted">Loading…</p>}

      <div className="table-wrap">
        <table>
          <thead><tr><th>Class</th><th>Medium</th><th>When</th><th>Price</th><th>Seats left</th><th>Visible</th><th><span className="sr">Actions</span></th></tr></thead>
          <tbody>
            {data?.map((c) => (
              <tr key={c.id} className={c.active ? '' : 'row-off'}>
                <td><b>{c.title}</b><br /><span className="muted small">{levelLabel(c.level)}, {c.instructor}</span></td>
                <td><span className="tag" style={{ ['--pigment' as string]: CATEGORIES[c.category].color }}>{CATEGORIES[c.category].label}</span></td>
                <td>{dayLabel(c.startsAt)}, {timeLabel(c.startsAt)}</td>
                <td>{money(c.priceCents)}</td>
                <td>{c.seatsLeft}/{c.capacity}</td>
                <td>{c.active ? 'Yes' : 'Hidden'}</td>
                <td className="actions">
                  <button className="btn btn-quiet" onClick={() => edit(c)}>Edit</button>
                  {c.active && <button className="btn btn-quiet" onClick={() => hide(c)}>Hide</button>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {editing && f && (
        <div className="modal-back" onMouseDown={(e) => e.target === e.currentTarget && setEditing(null)}>
          <form className="modal modal-wide" onSubmit={save} role="dialog" aria-modal="true" aria-labelledby="cls-title">
            <div className="modal-head">
              <h2 id="cls-title">{editing.id ? 'Edit class' : 'Add a class'}</h2>
              <button type="button" className="btn btn-quiet" onClick={() => setEditing(null)}>Close</button>
            </div>
            <label>Title<input required maxLength={120} value={f.title} onChange={(e) => set('title', e.target.value)} /></label>
            <label>Description<textarea required rows={3} maxLength={2000} value={f.description} onChange={(e) => set('description', e.target.value)} /></label>
            <div className="row2">
              <label>Medium
                <select value={f.category} onChange={(e) => set('category', e.target.value as Category)}>
                  {CATEGORY_ORDER.map((k) => <option key={k} value={k}>{CATEGORIES[k].label}</option>)}
                </select>
              </label>
              <label>Level
                <select value={f.level} onChange={(e) => set('level', e.target.value as Level)}>
                  <option value="ALL_LEVELS">All levels</option><option value="BEGINNER">Beginner</option><option value="INTERMEDIATE">Intermediate</option>
                </select>
              </label>
            </div>
            <div className="row2">
              <label>Teacher<input required value={f.instructor} onChange={(e) => set('instructor', e.target.value)} /></label>
              <label>Starts<input type="datetime-local" required value={f.startsAt} onChange={(e) => set('startsAt', e.target.value)} /></label>
            </div>
            <label>Location<input required value={f.location} onChange={(e) => set('location', e.target.value)} /></label>
            <div className="row3">
              <label>Minutes<input type="number" min={15} max={480} required value={f.durationMinutes} onChange={(e) => set('durationMinutes', Number(e.target.value))} /></label>
              <label>Price (₪)<input type="number" min={0} required value={f.priceCents / 100} onChange={(e) => set('priceCents', Math.round(Number(e.target.value) * 100))} /></label>
              <label>Capacity<input type="number" min={1} max={100} required value={f.capacity} onChange={(e) => set('capacity', Number(e.target.value))} /></label>
            </div>
            <label className="check"><input type="checkbox" checked={f.active} onChange={(e) => set('active', e.target.checked)} /> Visible on the public schedule</label>
            {problem && <p className="alert" role="alert">{problem}</p>}
            <button className="btn btn-solid btn-wide" disabled={busy}>{busy ? 'Saving…' : editing.id ? 'Save changes' : 'Add class'}</button>
          </form>
        </div>
      )}
    </>
  );
}
