import type { Category } from './api';

/** Each medium has its own pigment. The colour follows a class everywhere: filters, cards, admin charts. */
export const CATEGORIES: Record<Category, { label: string; color: string; ink: string; blurb: string }> = {
  PAINTING:     { label: 'Painting',     color: '#2F43D8', ink: '#FFFFFF', blurb: 'Oils and acrylics on canvas' },
  WATERCOLOR:   { label: 'Watercolor',   color: '#2E9BCB', ink: '#08202B', blurb: 'Water, paper, patience' },
  CERAMICS:     { label: 'Ceramics',     color: '#B8572F', ink: '#FFFFFF', blurb: 'Wheel and handbuilding' },
  DRAWING:      { label: 'Drawing',      color: '#2A2A33', ink: '#FFFFFF', blurb: 'Charcoal, pencil, ink' },
  ILLUSTRATION: { label: 'Illustration', color: '#E0486F', ink: '#FFFFFF', blurb: 'Digital on iPad' },
  KIDS:         { label: 'Kids',         color: '#F4C20D', ink: '#2A2200', blurb: 'Ages 6 to 10' },
};

export const CATEGORY_ORDER = Object.keys(CATEGORIES) as Category[];
