const BASE_URL = 'http://localhost:8080';

/**
 * Thin fetch wrapper: builds the full URL, attaches the JWT bearer
 * token (if present) to every request's Authorization header, and
 * throws a normalized Error carrying the backend's ApiError message
 * on any non-2xx response so callers can display it directly.
 */
async function request(path, { method = 'GET', body, auth = true } = {}) {
  const headers = { 'Content-Type': 'application/json' };

  if (auth) {
    const token = localStorage.getItem('token');
    if (token) headers.Authorization = `Bearer ${token}`;
  }

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const errBody = await res.json();
      if (errBody?.message) message = errBody.message;
    } catch {
      // response wasn't JSON - fall back to the generic message
    }
    const error = new Error(message);
    error.status = res.status;
    throw error;
  }

  if (res.status === 204) return null;
  return res.json();
}

export const api = {
  register: (email, password) =>
    request('/api/auth/register', { method: 'POST', body: { email, password }, auth: false }),

  login: (email, password) =>
    request('/api/auth/login', { method: 'POST', body: { email, password }, auth: false }),

  listEvents: () => request('/api/events', { auth: false }),

  getEvent: (eventId) => request(`/api/events/${eventId}`, { auth: false }),

  bookSeat: (seatId) => request('/api/bookings', { method: 'POST', body: { seatId } }),

  streamUrl: (eventId) => `${BASE_URL}/api/events/${eventId}/stream`,
};
