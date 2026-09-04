package com.routesync.backend.service;

import com.routesync.backend.dto.auth.TokenResponse;
import com.routesync.backend.entity.User;

/**
 * Authentication ke baad access aur refresh token
 * generate/manage karne ka contract.
 */
public interface TokenService {

    /**
     * User ke liye access token + refresh token generate karta hai.
     */
    TokenResponse generateTokens(User user);

    /**
     * Refresh token ko validate karke new access token generate karta hai.
     */
    TokenResponse refreshAccessToken(String refreshToken);

    /**
     * Refresh token ko revoke karta hai.
     */
    void revokeRefreshToken(String refreshToken);

    /**
     * User ke saare refresh tokens revoke/delete karta hai.
     */
    void revokeAllUserTokens(User user);
}