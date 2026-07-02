-- =====================================================================
-- Event Seat Booking Engine - Initial Schema Migration
-- Target: PostgreSQL 14+
-- =====================================================================
-- Notes on design decisions:
--   * All primary keys use BIGSERIAL/BIGINT for high-volume scalability.
--   * seats.version is the backbone of our Optimistic Locking strategy
--     (see Seat.java @Version). Every UPDATE to a seat row must include
--     "AND version = :expectedVersion" (Hibernate does this for us
--     automatically) and bump the version by 1. If zero rows are
--     affected, Hibernate throws ObjectOptimisticLockingFailureException,
--     which we translate into a 409 Conflict in Phase 3.
--   * ENUM-like columns are modeled as VARCHAR + CHECK constraints
--     rather than native PostgreSQL ENUM types, since native enums are
--     painful to alter later (ADD VALUE cannot run in a transaction in
--     older PG versions). JPA entities use @Enumerated(EnumType.STRING)
--     to map cleanly onto these.
-- =====================================================================

-- Drop in dependency order (useful for local dev resets; remove in prod migration tooling)
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS seats CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- =====================================================================
-- users
-- =====================================================================
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER'
                  CHECK (role IN ('USER', 'ADMIN')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON users (email);

-- =====================================================================
-- events
-- =====================================================================
CREATE TABLE events (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    event_date  TIMESTAMPTZ  NOT NULL,
    venue       VARCHAR(255) NOT NULL,
    total_seats INTEGER      NOT NULL CHECK (total_seats > 0),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_events_event_date ON events (event_date);

-- =====================================================================
-- seats
-- =====================================================================
-- The "version" column is the crux of the whole system: it is a plain
-- INTEGER that Hibernate silently manages via @Version. Never write to
-- it manually from application code.
-- =====================================================================
CREATE TABLE seats (
    id          BIGSERIAL PRIMARY KEY,
    event_id    BIGINT       NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    seat_number VARCHAR(10)  NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE'
                CHECK (status IN ('AVAILABLE', 'LOCKED', 'BOOKED')),
    price       NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    version     INTEGER      NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_seat_event_seatnumber UNIQUE (event_id, seat_number)
);

CREATE INDEX idx_seats_event_id ON seats (event_id);
CREATE INDEX idx_seats_event_status ON seats (event_id, status);

-- =====================================================================
-- bookings
-- =====================================================================
CREATE TABLE bookings (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    seat_id      BIGINT      NOT NULL REFERENCES seats (id) ON DELETE CASCADE,
    booking_time TIMESTAMPTZ NOT NULL DEFAULT now(),
    status       VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED'
                 CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    CONSTRAINT uq_booking_seat UNIQUE (seat_id)
    -- A seat can only ever have one active booking row tying it to a
    -- user; this is a secondary, database-level safety net that backs
    -- up the optimistic-locking check happening at the application tier.
);

CREATE INDEX idx_bookings_user_id ON bookings (user_id);
CREATE INDEX idx_bookings_seat_id ON bookings (seat_id);

-- =====================================================================
-- updated_at auto-touch trigger (applied to all four tables)
-- =====================================================================
CREATE OR REPLACE FUNCTION trigger_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_updated_at_users
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

CREATE TRIGGER set_updated_at_events
    BEFORE UPDATE ON events
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

CREATE TRIGGER set_updated_at_seats
    BEFORE UPDATE ON seats
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();
