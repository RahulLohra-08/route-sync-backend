package com.routesync.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Existing user profile update karne ke liye DTO.
 *
 * Yahan saare fields optional rakhe gaye hain.
 * User sirf required field update kar sakta hai.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    /**
     * User ka updated name.
     */
    @Size(
            min = 2,
            max = 100,
            message = "Name must be between 2 and 100 characters"
    )
    private String fullName;


    /**
     * Updated email.
     */
    @Email(message = "Please provide a valid email address")
    @Size(
            max = 150,
            message = "Email cannot exceed 150 characters"
    )
    private String email;


    /**
     * Updated phone number.
     */
    @Pattern(
            regexp = "^[+]?[0-9]{10,15}$",
            message = "Please provide a valid phone number"
    )
    private String phoneNumber;


    /**
     * Updated profile image URL.
     */
    private String profileImage;
}