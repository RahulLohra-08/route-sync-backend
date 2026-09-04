package com.routesync.backend.entity;

/**
 * OTP kis purpose ke liye use ho raha hai
 * usko define karta hai.
 */
public enum OtpPurpose {

    /**
     * User login ke liye OTP.
     */
    LOGIN,

    /**
     * New user registration verify karne ke liye OTP.
     */
    REGISTRATION,

    /**
     * Password reset ke liye OTP.
     */
    PASSWORD_RESET
}