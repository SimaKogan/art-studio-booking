import { Link } from 'react-router-dom';
import { api, type StudioClass } from '../api';
import { CATEGORIES, CATEGORY_ORDER } from '../categories';
import ClassCard from '../components/ClassCard';
import CoverArt from '../components/CoverArt';
import { dayLabel, money, timeLabel } from '../format';
import { useFetch } from '../hooks';

function Chip({ c, i }: { c: StudioClass; i: number }) {
  const cat = CATEGORIES[c.category];
  return (
    <Link to={`/classes/${c.id}`} className={`chip chip-${i}`} style={{ ['--pigment' as string]: cat.color, ['--on-pigment' as string]: cat.ink }}>
      <div className="chip-swatch"><CoverArt category={c.category} /><span>{cat.label}</span></div>
      <div className="chip-body">
        <b>{c.title}</b>
        <span>{dayLabel(c.startsAt)}, {timeLabel(c.startsAt)}</span>
        <span>{money(c.priceCents)}</span>
      </div>
    </Link>
  );
}

export default function Home() {
  const { data, error, loading } = useFetch(() => api<StudioClass[]>('/classes'), []);
  const upcoming = data ?? [];

  return (
    <>
      <section className="hero">
        <div className="wrap hero-in">
          <div className="hero-copy">
            <h1>Make something with your hands this week.</h1>
            <p className="lede">
              Painting, watercolor, ceramics and drawing in small groups, in the middle of town.
              Choose a session, pay online and turn up. Everything you need is on the table.
            </p>
            <div className="hero-cta">
              <Link className="btn btn-solid btn-lg" to="/classes">Browse classes</Link>
              <Link className="btn btn-line btn-lg" to="/register">Create an account</Link>
            </div>
          </div>

          <div className="fan" aria-label="Next sessions">
            {loading && <div className="chip chip-skel" aria-hidden="true" />}
            {!loading && upcoming.slice(0, 3).map((c, i) => <Chip key={c.id} c={c} i={i} />)}
            {error && <p className="alert">{error}</p>}
          </div>
        </div>
      </section>

      <section className="wrap section">
        <h2>Pick a medium</h2>
        <div className="swatches">
          {CATEGORY_ORDER.map((k) => (
            <Link key={k} to={`/classes?category=${k}`} className="swatch"
              style={{ ['--pigment' as string]: CATEGORIES[k].color, ['--on-pigment' as string]: CATEGORIES[k].ink }}>
              <CoverArt category={k} />
              <b>{CATEGORIES[k].label}</b>
              <span>{CATEGORIES[k].blurb}</span>
            </Link>
          ))}
        </div>
      </section>

      <section className="wrap section">
        <div className="section-head">
          <h2>Coming up</h2>
          <Link to="/classes" className="link">See every session</Link>
        </div>
        {!loading && upcoming.length === 0 && !error && (
          <p className="muted">No sessions are scheduled yet. Check back soon.</p>
        )}
        <div className="grid">
          {upcoming.slice(0, 6).map((c) => <ClassCard key={c.id} c={c} />)}
        </div>
      </section>

      <section className="wrap section">
        <h2>How booking works</h2>
        <ol className="steps">
          <li><b>Pick a session.</b> Filter by medium, level, day or budget, or ask the assistant in the corner.</li>
          <li><b>We hold your seats for 15 minutes.</b> Nobody else can take them while you check out.</li>
          <li><b>Pay and you are in.</b> Free cancellation up to 24 hours before the class.</li>
        </ol>
      </section>
    </>
  );
}
