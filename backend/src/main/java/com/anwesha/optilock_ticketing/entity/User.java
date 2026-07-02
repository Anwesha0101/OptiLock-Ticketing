package com.anwesha.optilock_ticketing.entity;

//import com.anwesha.optilockticketing.entity.enums.Role;

import com.anwesha.optilock_ticketing.enums.Role;
import com.anwesha.optilock_ticketing.enums.SeatStatus;
import com.anwesha.optilock_ticketing.enums.BookingStatus;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Application user. Passwords are never stored in plaintext -
 * {@code passwordHash} holds a BCrypt digest produced by Spring
 * Security's {@code PasswordEncoder} at registration time.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
