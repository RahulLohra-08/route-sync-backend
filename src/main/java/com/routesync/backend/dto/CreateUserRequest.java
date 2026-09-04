package com.routesync.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User create karne ke liye request DTO.
 *
 * DTO ka kaam:
 * - Client se required data lena
 * - Input validation karna
 * - Entity ko directly expose hone se bachana
 *
 * Client directly User entity ke sensitive fields
 * jaise role, active, googleId modify nahi kar sakta.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    /**
     * User ka full name.
     */
    @NotBlank(message = "Name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Name must be between 2 and 100 characters"
    )
    private String fullName;


    /**
     * User ka email.
     *
     * Email optional hai because future mein
     * OTP based user sirf phone number se register kar sakta hai.
     */
    @Email(message = "Please provide a valid email address")
    @Size(
            max = 150,
            message = "Email cannot exceed 150 characters"
    )
    private String email;


    /**
     * User ka phone number.
     *
     * Basic validation abhi rakhenge.
     * Future mein proper international phone validation
     * add kar sakte hain.
     */
    @Pattern(
            regexp = "^[+]?[0-9]{10,15}$",
            message = "Please provide a valid phone number"
    )
    private String phoneNumber;


    /**
     * Profile image optional hai.
     */
    private String profileImage;
}