package com.routesync.backend.security;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

/**
 * JWT se related saare operations ka contract.
 *
 * Is interface ke through hum:
 * - Access token generate karenge
 * - Refresh token generate karenge
 * - Token se username/subject nikalenge
 * - Token validate karenge
 * - Token expiry check karenge
 */
public interface JwtService {

    /**
     * UserDetails ke basis par access token generate karta hai.
     */
    String generateAccessToken(UserDetails userDetails);

    /**
     * UserDetails ke basis par refresh token generate karta hai.
     */
    String generateRefreshToken(UserDetails userDetails);

    /**
     * Additional claims ke saath access token generate karta hai.
     */
    String generateAccessToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    );

    /**
     * JWT token se subject/username extract karta hai.
     */
    String extractUsername(String token);

    /**
     * JWT token valid hai ya nahi check karta hai.
     */
    boolean isTokenValid(String token, UserDetails userDetails);

    /**
     * JWT token expired hai ya nahi check karta hai.
     */
    boolean isTokenExpired(String token);

    /**
     * Access token ka remaining validity milliseconds mein return karta hai.
     */
    long getAccessTokenExpiration();
}