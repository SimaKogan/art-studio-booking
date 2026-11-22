export type Category = 'PAINTING' | 'WATERCOLOR' | 'CERAMICS' | 'DRAWING' | 'ILLUSTRATION' | 'KIDS';
export type Level = 'BEGINNER' | 'INTERMEDIATE' | 'ALL_LEVELS';
export type Role = 'USER' | 'ADMIN';
export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED';

export interface User { id: number; email: string; fullName: string; role: Role }
export interface AuthResponse { token: string; user: User }

export interface StudioClass {
  id: number; title: string; description: string; category: Category; level: Level;
  instructor: string; location: string; startsAt: string; durationMinutes: number;
  priceCents: number; capacity: number; seatsLeft: number; active: boolean;
}
export interface ClassInput {
  title: string; description: string; category: Category; level: Level; instructor: string;
  location: string; startsAt: string; durationMinutes: number; priceCents: number; capacity: number; active: boolean;
}

export interface Booking {
  id: number; classId: number; classTitle: string; category: Category; startsAt: string; location: string;
  seats: number; totalCents: number; status: BookingStatus; createdAt: string; holdExpiresAt: string | null;
  userName: string; userEmail: string;
}

export interface PaymentInput { bookingId: number; cardholder: string; cardNumber: string; expiry: string; cvc: string }
export interface Payment {
  id: number; bookingId: number; status: 'SUCCEEDED' | 'FAILED' | 'REFUNDED'; amountCents: number;
  reference: string | null; cardBrand: string; last4: string; message: string; bookingStatus: BookingStatus;
}

export interface ChatTurn { role: 'user' | 'assistant'; text: string }
export interface ChatResponse { reply: string; suggestions: StudioClass[]; provider: string }

export interface Stats {
  revenueCents: number; confirmedBookings: number; pendingBookings: number; upcomingClasses: number;
  averageOccupancy: number; users: number;
  classes: { classId: number; title: string; category: Category; startsAt: string; capacity: number; booked: number; revenueCents: number }[];
  categories: { category: Category; bookings: number; revenueCents: number }[];
}

export class ApiError extends Error {
  constructor(message: string, public status: number) {
    super(message);
  }
}

const TOKEN_KEY = 'asb.token';
let token: string | null = null;
try { token = localStorage.getItem(TOKEN_KEY); } catch { /* storage unavailable */ }

export function getToken() { return token; }
export function setToken(t: string | null) {
  token = t;
  try { if (t) localStorage.setItem(TOKEN_KEY, t); else localStorage.removeItem(TOKEN_KEY); } catch { /* ignore */ }
}

const BASE = (import.meta.env.VITE_API_URL as string | undefined) ?? '';

export async function api<T>(path: string, init: { method?: string; body?: unknown } = {}): Promise<T> {
  const headers: Record<string, string> = {};
  if (init.body !== undefined) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = `Bearer ${token}`;

  let res: Response;
  try {
    res = await fetch(`${BASE}/api${path}`, {
      method: init.method ?? 'GET',
      headers,
      body: init.body === undefined ? undefined : JSON.stringify(init.body),
    });
  } catch {
    throw new ApiError('Cannot reach the server. Check that the backend is running.', 0);
  }
  if (res.status === 204) return undefined as T;
  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) {
    throw new ApiError((data && (data.detail || data.message)) || `Request failed (${res.status})`, res.status);
  }
  return data as T;
}

export const errorMessage = (e: unknown) => (e instanceof Error ? e.message : 'Something went wrong');
