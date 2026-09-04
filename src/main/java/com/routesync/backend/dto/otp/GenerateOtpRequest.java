package com.routesync.backend.dto.otp;

import com.routesync.backend.entity.OtpPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;

import lombok.*;

/**
 * Mobile number par OTP send karne ke liye request.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateOtpRequest {

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
     * OTP ka purpose.
     */
    @NotNull(message = "OTP purpose is required")
    private OtpPurpose purpose;
}