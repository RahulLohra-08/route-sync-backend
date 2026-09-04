package com.routesync.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Access token expire hone ke baad new access token
 * lene ke liye client refresh token bhejega.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    /**
     * Existing refresh token.
     */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}