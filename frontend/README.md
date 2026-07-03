# OptiLock — Frontend

React (Vite) + Tailwind client for the OptiLock Ticketing backend
(`com.anwesha.optilock_ticketing`). Talks to the API at
`http://localhost:8080` — see `src/api/client.js` to change that.

## Run it
```bash
npm install
npm run dev
```
Opens on `http://localhost:5173`.

## What's here
- `src/api/client.js` — fetch wrapper, attaches the JWT bearer token
  from `localStorage` to every authenticated request, matches your
  backend's `AuthDtos` / `EventDtos` / `BookingDtos` response shapes exactly.
- `src/context/AuthContext.jsx` — session state (register/login/logout).
- `src/pages/DashboardPage.jsx` — `GET /api/events`, event listing.
- `src/pages/EventPage.jsx` — `GET /api/events/{id}` for the seat grid,
  plus a native `EventSource` subscription to
  `GET /api/events/{id}/stream` for live seat-status updates, and
  `POST /api/bookings` for checkout.
- `src/pages/LoginPage.jsx` / `RegisterPage.jsx` — `POST /api/auth/login`
  and `/register`.

## Design
"Box office at dusk" theme: charcoal-plum background, marquee-gold and
velvet-crimson accents, Bebas Neue display type, IBM Plex Mono for seat
numbers. Seats render as small ticket-stub shapes grouped into rows
under a glowing "STAGE" bar (`src/components/SeatGrid.jsx`), color-coded
by status: gold = available, crimson = booked, slate = locked.

## Trying the live sync
Open the same event in two tabs, sign in as two different users in each,
and book a seat in one tab — the other tab's grid updates within
milliseconds via SSE, no refresh needed. Click the same seat in both
tabs at once and you'll see your backend's optimistic-locking 409 land
in one of the two.
