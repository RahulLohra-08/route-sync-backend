package com.routesync.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OTP verify karne ke liye request DTO.
 *
 * Client mobile number + OTP send karega.
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
    @NotBlank(message = "Mobile number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Please enter a valid Indian mobile number"
    )
    private String mobileNumber;

    /**
     * User ko mila hua OTP.
     *
     * OTP normally 6 digits ka hoga.
     */
    @NotBlank(message = "OTP is required")
    @Pattern(
            regexp = "^\\d{6}$",
            message = "OTP must be 6 digits"
    )
    private String otp;
}