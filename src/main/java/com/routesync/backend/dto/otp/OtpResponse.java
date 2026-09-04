package com.routesync.backend.dto.otp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * OTP related API response ko represent karta hai.
 */
@Getter
@Builder
@AllArgsConstructor
public class OtpResponse {

    /**
     * API response ka message.
     */
    private String message;

    /**
     * Development/testing ke liye generated OTP.
     *
     * Production mein ise response mein nahi bhejna hai.
     */
    private String otp;
}