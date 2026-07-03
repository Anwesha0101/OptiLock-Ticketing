package com.anwesha.optilock_ticketing.service;

import com.anwesha.optilock_ticketing.dto.AuthDtos.AuthResponse;
import com.anwesha.optilock_ticketing.dto.AuthDtos.LoginRequest;
import com.anwesha.optilock_ticketing.dto.AuthDtos.RegisterRequest;
import com.anwesha.optilock_ticketing.entity.User;
import com.anwesha.optilock_ticketing.enums.Role;
import com.anwesha.optilock_ticketing.repository.UserRepository;
import com.anwesha.optilock_ticketing.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user registration and login, delegating credential
 * verification to Spring Security's AuthenticationManager and JWT
 * issuance to JwtTokenProvider.
 *
 * Adjust the com.anwesha.optilock_ticketing.entity / .entity.enums /
 * .repository / .security import paths above if your existing
 * classes live in different sub-packages.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Registers a new user with a BCrypt-encoded password and returns
     * a freshly minted JWT so the client is signed in immediately.
     *
     * @throws IllegalArgumentException if the email is already taken
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        User saved = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(saved.getId(), saved.getEmail(), saved.getRole().name());
        return AuthResponse.bearer(token, saved.getId(), saved.getEmail(), saved.getRole().name());
    }

    /**
     * Authenticates an existing user's credentials via the
     * AuthenticationManager (which in turn uses PasswordEncoder to
     * compare the raw password against the stored hash) and, on
     * success, returns a new JWT.
     *
     * @throws org.springframework.security.core.AuthenticationException
     *         if the email/password combination is invalid
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return AuthResponse.bearer(token, user.getId(), user.getEmail(), user.getRole().name());
    }
}
