-- =====================================================================
-- V2__seed_sample_data.sql
-- Seeds 2 sample events, each with 20 AVAILABLE seats (A1-A20).
-- =====================================================================

-- =====================================================================
-- Events
-- =====================================================================
INSERT INTO events (name, event_date, venue, total_seats)
VALUES
    ('Spring Boot Conference 2026', '2026-09-15 09:00:00+05:30', 'Bangalore', 20),
    ('Java Summit 2026',            '2026-10-20 09:00:00+05:30', 'Hyderabad', 20);

-- =====================================================================
-- Seats: A1-A20 for "Spring Boot Conference 2026"
-- =====================================================================
INSERT INTO seats (event_id, seat_number, status, price, version)
SELECT
    e.id,
    'A' || gs.n,
    'AVAILABLE',
    999.00,
    0
FROM events e
CROSS JOIN generate_series(1, 20) AS gs(n)
WHERE e.name = 'Spring Boot Conference 2026';

-- =====================================================================
-- Seats: A1-A20 for "Java Summit 2026"
-- =====================================================================
INSERT INTO seats (event_id, seat_number, status, price, version)
SELECT
    e.id,
    'A' || gs.n,
    'AVAILABLE',
    999.00,
    0
FROM events e
CROSS JOIN generate_series(1, 20) AS gs(n)
WHERE e.name = 'Java Summit 2026';
