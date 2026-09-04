package com.routesync.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Google authentication ke time client se aane wala data.
 *
 * Production implementation mein Google ID token ko
 * backend par verify karke actual Google "sub" obtain karna chahiye.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleAuthRequest {

    /**
     * Google ka unique user identifier.
     *
     * Google OAuth mein normally "sub" ke naam se milta hai.
     */
    @NotBlank(message = "Google ID is required")
    private String googleId;

    /**
     * Google account se user ka full name.
     */
    @NotBlank(message = "Full name is required")
    private String fullName;

    /**
     * Google account ka email.
     */
    private String email;

    /**
     * Google profile image URL.
     */
    private String profileImage;
}