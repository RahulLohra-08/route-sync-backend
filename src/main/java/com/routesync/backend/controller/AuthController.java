package com.routesync.backend.controller;

import com.routesync.backend.dto.auth.AuthResponse;
import com.routesync.backend.dto.auth.GoogleAuthRequest;
import com.routesync.backend.dto.auth.OtpLoginRequest;
import com.routesync.backend.dto.auth.RefreshTokenRequest;
import com.routesync.backend.dto.auth.TokenResponse;
import com.routesync.backend.service.AuthService;
import com.routesync.backend.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication related APIs.
 *
 * OTP, Google aur JWT authentication
 * ke endpoints yahan handle honge.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final TokenService tokenService;

    /**
     * OTP login / registration.
     */

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponse> verifyOtp(
            @Valid @RequestBody OtpLoginRequest request
    ) {

        return ResponseEntity.ok(
                authService.verifyOtp(request)
        );
    }

    /**
     * Google authentication.
     */
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> authenticateGoogle(
            @Valid @RequestBody GoogleAuthRequest request
    ) {

        return ResponseEntity.ok(
                authService.authenticateGoogle(request)
        );
    }

    /**
     * Expired access token ke bad
     * refresh token se new access token generate karta hai.
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshAccessToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {

        return ResponseEntity.ok(
                tokenService.refreshAccessToken(
                        request.getRefreshToken()
                )
        );
    }

    /**
     * Refresh token ko revoke karke logout karta hai.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {

        tokenService.revokeRefreshToken(
                request.getRefreshToken()
        );

        return ResponseEntity.noContent().build();
    }
}