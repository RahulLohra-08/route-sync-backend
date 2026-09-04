package com.routesync.backend.service;

import com.routesync.backend.entity.OtpPurpose;

import java.time.LocalDateTime;

/**
 * OTP related business logic ke liye service interface.
 *
 * OTP flow:
 * Mobile Number -> Generate OTP -> Verify OTP
 */
public interface OtpService {

    /**
     * Mobile number ke liye new OTP generate karega.
     *
     * New user ke case mein abhi user exist nahi bhi kar sakta.
     *
     * @param phoneNumber user ka mobile number
     * @param purpose OTP ka purpose
     * @return generated OTP
     */
    String generateOtp(
            String phoneNumber,
            OtpPurpose purpose
    );

    /**
     * Mobile number ke against OTP verify karega.
     *
     * @param phoneNumber user ka mobile number
     * @param otp user dwara entered OTP
     * @param purpose OTP ka purpose
     */
    void verifyOtp(
            String phoneNumber,
            String otp,
            OtpPurpose purpose
    );
}