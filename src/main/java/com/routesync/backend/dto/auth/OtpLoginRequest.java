package com.routesync.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OTP login ke time client se aane wala request.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpLoginRequest {

    /**
     * User ka mobile number.
     */
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone number must contain exactly 10 digits"
    )
    private String phoneNumber;

    /**
     * User ke mobile par received OTP.
     */
    @NotBlank(message = "OTP is required")
    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "OTP must contain exactly 6 digits"
    )
    private String otp;

    /**
     * OTP registration ke time user ka naam.
     *
     * Existing user ke login mein optional rahega.
     */
    private String fullName;
}