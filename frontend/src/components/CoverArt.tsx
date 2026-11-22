import type { Category } from '../api';

/** Abstract cover per medium, drawn in white over the category pigment. No stock imagery needed. */
export default function CoverArt({ category }: { category: Category }) {
  const w = '#fff';
  let art;
  switch (category) {
    case 'PAINTING':
      art = (<>
        <circle cx="70" cy="70" r="46" fill={w} fillOpacity=".16" />
        <circle cx="130" cy="52" r="38" fill={w} fillOpacity=".22" />
        <circle cx="105" cy="96" r="30" fill={w} fillOpacity=".14" />
        <path d="M200 100 C 230 20, 270 120, 310 30" stroke={w} strokeOpacity=".5" strokeWidth="10" strokeLinecap="round" fill="none" />
      </>);
      break;
    case 'WATERCOLOR':
      art = (<>
        <path d="M0 78 C 40 40, 80 110, 120 70 S 200 40, 240 76 S 300 100, 320 60 V120 H0Z" fill={w} fillOpacity=".24" />
        <path d="M0 98 C 50 70, 90 120, 150 92 S 250 70, 320 100 V120 H0Z" fill={w} fillOpacity=".2" />
        <circle cx="250" cy="28" r="16" fill={w} fillOpacity=".4" />
      </>);
      break;
    case 'CERAMICS':
      art = (<>
        <path d="M70 18 H130 C 120 40, 150 52, 148 84 C 146 106, 128 112, 100 112 C 72 112, 54 106, 52 84 C 50 52, 80 40, 70 18Z" fill={w} fillOpacity=".22" />
        <ellipse cx="100" cy="18" rx="30" ry="6" fill={w} fillOpacity=".35" />
        <path d="M56 70 C 80 80, 120 80, 146 70" stroke={w} strokeOpacity=".45" strokeWidth="3" fill="none" />
        <path d="M200 112 C 200 70, 280 70, 280 112Z" fill={w} fillOpacity=".16" />
      </>);
      break;
    case 'DRAWING':
      art = (<g stroke={w} strokeOpacity=".28" strokeWidth="2">
        {Array.from({ length: 16 }, (_, i) => <line key={i} x1={i * 22 - 40} y1="130" x2={i * 22 + 40} y2="-10" />)}
        <circle cx="240" cy="62" r="34" fill={w} fillOpacity=".12" stroke="none" />
      </g>);
      break;
    case 'ILLUSTRATION':
      art = (<>
        {Array.from({ length: 5 }, (_, r) => Array.from({ length: 14 }, (_, c) =>
          <circle key={`${r}-${c}`} cx={20 + c * 22} cy={18 + r * 22} r="2.6" fill={w} fillOpacity=".35" />))}
        <path d="M30 96 C 80 20, 130 130, 190 54 S 270 30, 300 80" stroke={w} strokeOpacity=".7" strokeWidth="6" strokeLinecap="round" fill="none" />
      </>);
      break;
    case 'KIDS':
      art = (<g fill="#2A2200" fillOpacity=".14">
        <circle cx="60" cy="64" r="34" />
        <circle cx="130" cy="38" r="20" />
        <circle cx="190" cy="82" r="28" />
        <circle cx="262" cy="44" r="38" />
        <circle cx="300" cy="104" r="14" />
      </g>);
      break;
  }
  return (
    <svg className="cover" viewBox="0 0 320 120" preserveAspectRatio="xMidYMid slice" aria-hidden="true">
      {art}
    </svg>
  );
}
