import { Link } from 'react-router-dom';
import type { StudioClass } from '../api';
import { CATEGORIES } from '../categories';
import { dayLabel, levelLabel, money, timeLabel } from '../format';
import CoverArt from './CoverArt';
import SeatMeter from './SeatMeter';

export default function ClassCard({ c }: { c: StudioClass }) {
  const cat = CATEGORIES[c.category];
  return (
    <Link to={`/classes/${c.id}`} className="card" style={{ ['--pigment' as string]: cat.color, ['--on-pigment' as string]: cat.ink }}>
      <div className="card-top">
        <CoverArt category={c.category} />
        <span className="card-cat">{cat.label}</span>
      </div>
      <div className="card-body">
        <h3>{c.title}</h3>
        <p className="card-when">{dayLabel(c.startsAt)}, {timeLabel(c.startsAt)}</p>
        <p className="card-sub">{levelLabel(c.level)} with {c.instructor}</p>
        <div className="card-foot">
          <SeatMeter capacity={c.capacity} seatsLeft={c.seatsLeft} />
          <strong className="price">{money(c.priceCents)}</strong>
        </div>
      </div>
    </Link>
  );
}
