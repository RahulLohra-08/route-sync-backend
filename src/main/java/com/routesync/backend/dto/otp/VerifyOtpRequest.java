package com.routesync.backend.dto.otp;

import com.routesync.backend.entity.OtpPurpose;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import lombok.*;

/**
 * OTP verification request.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {

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
     * User dwara enter kiya gaya OTP.
     */
    @NotBlank(message = "OTP is required")
    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "OTP must contain exactly 6 digits"
    )
    private String otp;

    /**
     * OTP purpose.
     */
    @NotNull(message = "OTP purpose is required")
    private OtpPurpose purpose;
}