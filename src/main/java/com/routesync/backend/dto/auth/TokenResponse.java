package com.routesync.backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Access token aur refresh token ko client ko return karne ke liye DTO.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    /**
     * Short-lived JWT access token.
     */
    private String accessToken;

    /**
     * Long-lived refresh token.
     */
    private String refreshToken;

    /**
     * Token type normally "Bearer" hoga.
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access token kitne milliseconds ke liye valid hai.
     */
    private long expiresIn;
}