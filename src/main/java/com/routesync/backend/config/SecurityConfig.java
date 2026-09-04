package com.routesync.backend.config;

import com.routesync.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                /*
                 * JWT based REST API ke liye CSRF disable.
                 */
                .csrf(csrf -> csrf.disable())

                /*
                 * JWT stateless authentication.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        /*
                         * Authentication endpoints public.
                         */
                        .requestMatchers(
                                "/api/v1/auth/**"
                        ).permitAll()

                        /*
                         * ADMIN ONLY
                         */
                        .requestMatchers(
                                "/api/v1/admin/**",
                                "/api/v1/users",
                                "/api/v1/users/email/**",
                                "/api/v1/users/phone/**"
                        ).hasRole("ADMIN")

                        /*
                         * DRIVER + ADMIN
                         */
                        .requestMatchers("/api/v1/driver/**").hasAnyRole("DRIVER","ADMIN")

                        /*
                         * PASSENGER + DRIVER + ADMIN
                         */
                        .requestMatchers(
                                "/api/v1/passenger/**"
                        ).hasAnyRole(
                                "PASSENGER",
                                "DRIVER",
                                "ADMIN"
                        )

                        /*
                         * /users/me will be accessible to authenticated users.
                         */
                        .requestMatchers(
                                "/api/v1/users/me"
                        ).authenticated()

                        /*
                         * Direct /users/{id} operations are ADMIN only.
                         */
                        .requestMatchers(
                                "/api/v1/users/*"
                        ).hasRole("ADMIN")

                        /*
                         * Baaki protected endpoints.
                         */
                        .anyRequest().authenticated()
                )

                /*
                 * JWT filter.
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}