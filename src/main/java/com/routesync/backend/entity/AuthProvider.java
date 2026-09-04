package com.routesync.backend.entity;

/**
 * User kis authentication method se login karta hai
 * usko represent karta hai.
 */
public enum AuthProvider {

    // Google OAuth ke through login
    GOOGLE,

    // Mobile number + OTP ke through login
    OTP
}