package com.anwesha.optilock_ticketing.enums;

/**
 * Authorization role assigned to a {@code User}.
 * Mapped to the database as a plain VARCHAR via
 * {@code @Enumerated(EnumType.STRING)} so the column stays
 * human-readable and resilient to enum re-ordering.
 */
public enum Role {
    USER,
    ADMIN
}
