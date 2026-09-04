package com.routesync.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OTP send karne ke liye request DTO.
 *
 * Client sirf mobile number send karega.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendOtpRequest {

    /**
     * User ka mobile number.
     *
     * Example:
     * 9876543210
     */
    @NotBlank(message = "Mobile number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Please enter a valid Indian mobile number"
    )
    private String mobileNumber;
}