import { useSearchParams } from 'react-router-dom';
import { api, type Category, type Level, type StudioClass } from '../api';
import { CATEGORIES, CATEGORY_ORDER } from '../categories';
import ClassCard from '../components/ClassCard';
import { useFetch } from '../hooks';

export default function Classes() {
  const [params, setParams] = useSearchParams();
  const category = (params.get('category') as Category | null) ?? '';
  const level = (params.get('level') as Level | null) ?? '';
  const q = params.get('q') ?? '';
  const maxPrice = params.get('maxPrice') ?? '';

  const set = (key: string, value: string) => {
    const next = new URLSearchParams(params);
    if (value) next.set(key, value); else next.delete(key);
    setParams(next, { replace: true });
  };

  const { data, error, loading } = useFetch(() => {
    const qs = new URLSearchParams();
    if (category) qs.set('category', category);
    if (level) qs.set('level', level);
    if (q) qs.set('q', q);
    if (maxPrice) qs.set('maxPrice', maxPrice);
    return api<StudioClass[]>(`/classes?${qs}`);
  }, [category, level, q, maxPrice]);

  const filtered = !!(category || level || q || maxPrice);

  return (
    <div className="wrap page">
      <h1>Classes</h1>

      <div className="filters" role="group" aria-label="Filter by medium">
        <button className={`pill ${category === '' ? 'pill-on' : ''}`} onClick={() => set('category', '')}>All</button>
        {CATEGORY_ORDER.map((k) => (
          <button key={k} aria-pressed={category === k}
            className={`pill ${category === k ? 'pill-on' : ''}`}
            style={{ ['--pigment' as string]: CATEGORIES[k].color, ['--on-pigment' as string]: CATEGORIES[k].ink }}
            onClick={() => set('category', category === k ? '' : k)}>
            <i className="dot" />{CATEGORIES[k].label}
          </button>
        ))}
      </div>

      <div className="filter-row">
        <label>Search
          <input type="search" value={q} placeholder="Title, teacher or keyword" onChange={(e) => set('q', e.target.value)} />
        </label>
        <label>Level
          <select value={level} onChange={(e) => set('level', e.target.value)}>
            <option value="">Any level</option>
            <option value="BEGINNER">Beginner</option>
            <option value="INTERMEDIATE">Intermediate</option>
          </select>
        </label>
        <label>Max price
          <select value={maxPrice} onChange={(e) => set('maxPrice', e.target.value)}>
            <option value="">Any price</option>
            <option value="120">Up to ₪120</option>
            <option value="180">Up to ₪180</option>
            <option value="250">Up to ₪250</option>
          </select>
        </label>
      </div>

      {error && <p className="alert" role="alert">{error}</p>}
      {loading && !data && <p className="muted">Loading sessions…</p>}

      {data && data.length === 0 && (
        <div className="empty">
          <h2>No sessions match</h2>
          <p className="muted">Try a different medium or a higher price limit.</p>
          {filtered && <button className="btn btn-line" onClick={() => setParams({}, { replace: true })}>Clear filters</button>}
        </div>
      )}
      <div className="grid">
        {data?.map((c) => <ClassCard key={c.id} c={c} />)}
      </div>
    </div>
  );
}
