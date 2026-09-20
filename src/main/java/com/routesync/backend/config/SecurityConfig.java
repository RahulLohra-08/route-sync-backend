package com.routesync.backend.config;

import com.routesync.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
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

                // Enable CORS for browser/web-app requests
                .cors(cors -> {})

                /*
                 * JWT stateless authentication.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // CORS preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        /*
                         * Authentication endpoints public.
                         */
                        .requestMatchers(
                                "/api/v1/auth/**", "/public/**"
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


                        // Driver APIs
                        .requestMatchers("/api/v1/drivers/**")
                        .authenticated()

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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONSuse"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept"
        ));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}