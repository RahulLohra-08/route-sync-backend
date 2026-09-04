package com.routesync.backend.dto.auth;

import com.routesync.backend.entity.AuthProvider;
import com.routesync.backend.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Successful authentication ke baad client ko
 * return hone wala complete response.
 *
 * Is response mein:
 * - User information
 * - Access token
 * - Refresh token
 * dono return honge.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * Authenticated user ka UUID.
     */
    private UUID userId;

    /**
     * User ka full name.
     */
    private String fullName;

    /**
     * User ka email.
     */
    private String email;

    /**
     * User ka phone number.
     */
    private String phoneNumber;

    /**
     * User ka profile image.
     */
    private String profileImage;

    /**
     * User ka authentication provider.
     *
     * OTP / GOOGLE
     */
    private AuthProvider authProvider;

    /**
     * User ka role.
     *
     * PASSENGER / DRIVER / ADMIN
     */
    private UserRole role;

    /**
     * Account active hai ya nahi.
     */
    private Boolean active;

    /**
     * JWT access token.
     *
     * Short-lived token hota hai.
     */
    private String accessToken;

    /**
     * JWT refresh token.
     *
     * Long-lived token hota hai.
     */
    private String refreshToken;

    /**
     * Token type.
     *
     * Normally "Bearer".
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access token ki validity milliseconds mein.
     */
    private long expiresIn;

    /**
     * Human-readable success message.
     */
    private String message;
}