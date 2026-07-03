package com.anwesha.optilock_ticketing.security;

import com.anwesha.optilock_ticketing.security.JwtAuthenticationFilter;
import com.anwesha.optilock_ticketing.security.RestAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Modern (Spring Security 6 / Boot 3) lambda-DSL SecurityFilterChain.
 * No WebSecurityConfigurerAdapter, no XML - just an annotation-driven bean.
 *
 * Access rules:
 *   - PUBLIC:    auth (login/register), GET on events/seats (browsing),
 *                the SSE stream endpoint, actuator health.
 *   - PROTECTED: everything under /api/bookings/** requires a valid JWT.
 *   - Everything else not explicitly listed defaults to authenticated,
 *     which is the safe default for an evolving API surface.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Stateless JWT API: no cookies/browser forms involved, so CSRF
            // protection (which defends against cookie-based session riding)
            // doesn't apply here and would only get in the way of clients.
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                // --- Public endpoints ---
                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events/**", "/api/events/*/seats").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events/*/stream").permitAll() // SSE stream
                .requestMatchers("/actuator/health").permitAll()

                // --- Protected endpoints ---
                .requestMatchers("/api/bookings/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/events/**").hasRole("ADMIN")

                // Anything else defaults to requiring authentication
                .anyRequest().authenticated()
            )
            // Insert our stateless JWT filter where Spring would normally
            // run its session-based username/password filter.
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Permissive CORS for local development against the Vite dev server.
     * Tighten allowedOrigins to your real frontend domain(s) in production.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
