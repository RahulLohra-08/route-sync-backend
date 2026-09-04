package com.routesync.backend.service;

import com.routesync.backend.dto.auth.AuthResponse;
import com.routesync.backend.dto.auth.GoogleAuthRequest;
import com.routesync.backend.dto.auth.OtpLoginRequest;

import java.util.UUID;

/**
 * Authentication se related business operations.
 */
public interface AuthService {

    /**
     * OTP verify karke user ko authenticate karega.
     *
     * Agar user already exist karta hai:
     * -> Login
     *
     * Agar user exist nahi karta:
     * -> New PASSENGER user create
     */
    AuthResponse verifyOtp(OtpLoginRequest request);

    /**
     * Google authentication handle karega.
     *
     * Existing googleId wale user ko login karega.
     * New Google user ko PASSENGER ke form mein create karega.
     */
    AuthResponse authenticateGoogle(GoogleAuthRequest request);

    /**
     * User account ko deactivate karega.
     *
     * Hard delete nahi hoga.
     */
    void deactivateUser(UUID userId);
}