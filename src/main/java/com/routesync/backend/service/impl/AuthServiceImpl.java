package com.routesync.backend.service.impl;

import com.routesync.backend.dto.auth.AuthResponse;
import com.routesync.backend.dto.auth.GoogleAuthRequest;
import com.routesync.backend.dto.auth.OtpLoginRequest;
import com.routesync.backend.dto.auth.TokenResponse;
import com.routesync.backend.entity.AuthProvider;
import com.routesync.backend.entity.OtpPurpose;
import com.routesync.backend.entity.User;
import com.routesync.backend.exception.DuplicateResourceException;
import com.routesync.backend.exception.ResourceNotFoundException;
import com.routesync.backend.repository.UserRepository;
import com.routesync.backend.service.AuthService;
import com.routesync.backend.service.OtpService;
import com.routesync.backend.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Authentication ki actual business logic.
 *
 * OTP aur Google authentication successful hone ke baad
 * JWT access + refresh token generate karta hai.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final OtpService otpService;

    private final TokenService tokenService;

    /**
     * OTP verify karke user authenticate karega.
     *
     * Existing user:
     * -> Login
     * -> JWT generate
     *
     * New user:
     * -> PASSENGER create
     * -> JWT generate
     */
    @Override
    public AuthResponse verifyOtp(OtpLoginRequest request) {

        /*
         * STEP 1
         *
         * OTP verify karenge.
         *
         * OTP invalid hua to yahin exception throw hoga.
         */
        otpService.verifyOtp(
                request.getPhoneNumber(),
                request.getOtp(),
                OtpPurpose.LOGIN
        );

        /*
         * STEP 2
         *
         * OTP successfully verify hone ke baad
         * phone number ke basis par user find karenge.
         */
        User user = userRepository
                .findByPhoneNumber(request.getPhoneNumber())
                .orElse(null);

        /*
         * STEP 3
         *
         * Existing user.
         */
        if (user != null) {

            /*
             * Deactivated user login nahi kar sakta.
             */
            if (!Boolean.TRUE.equals(user.getActive())) {

                throw new IllegalStateException(
                        "User account is deactivated"
                );
            }

            /*
             * Authentication successful.
             *
             * Ab JWT generate karenge.
             */
            return buildAuthResponse(
                    user,
                    "OTP login successful"
            );
        }

        /*
         * STEP 4
         *
         * New user registration ke liye full name required hai.
         */
        if (request.getFullName() == null
                || request.getFullName().isBlank()) {

            throw new IllegalArgumentException(
                    "Full name is required for new user registration"
            );
        }

        /*
         * STEP 5
         *
         * New PASSENGER user create karenge.
         */
        User newUser = User.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .authProvider(AuthProvider.OTP)
                .active(true)
                .build();

        /*
         * STEP 6
         *
         * Database mein user save karenge.
         */
        User savedUser = userRepository.save(newUser);

        /*
         * STEP 7
         *
         * User create + authentication successful.
         *
         * JWT tokens generate karenge.
         */
        return buildAuthResponse(
                savedUser,
                "OTP verified and account created successfully"
        );
    }

    /**
     * Google authentication handle karega.
     *
     * Existing Google user:
     * -> Login + JWT
     *
     * New Google user:
     * -> PASSENGER create + JWT
     */
    @Override
    public AuthResponse authenticateGoogle(
            GoogleAuthRequest request
    ) {

        /*
         * STEP 1
         *
         * Google ID ke basis par existing user search karenge.
         */
        User user = userRepository
                .findByGoogleId(request.getGoogleId())
                .orElse(null);

        /*
         * STEP 2
         *
         * Existing Google user.
         */
        if (user != null) {

            if (!Boolean.TRUE.equals(user.getActive())) {

                throw new IllegalStateException(
                        "User account is deactivated"
                );
            }

            /*
             * Existing user authenticated.
             *
             * JWT generate karenge.
             */
            return buildAuthResponse(
                    user,
                    "Google authentication successful"
            );
        }

        /*
         * STEP 3
         *
         * First-time Google login.
         *
         * Email available hai to duplicate account check karenge.
         */
        if (request.getEmail() != null
                && !request.getEmail().isBlank()) {

            User existingEmailUser = userRepository
                    .findByEmail(request.getEmail())
                    .orElse(null);

            if (existingEmailUser != null) {

                throw new DuplicateResourceException(
                        "An account already exists with email: "
                                + request.getEmail()
                );
            }
        }

        /*
         * STEP 4
         *
         * New Google user create karenge.
         */
        User newUser = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .profileImage(request.getProfileImage())
                .googleId(request.getGoogleId())
                .authProvider(AuthProvider.GOOGLE)
                .active(true)
                .build();

        /*
         * STEP 5
         *
         * Database mein save.
         */
        User savedUser = userRepository.save(newUser);

        /*
         * STEP 6
         *
         * JWT generate karke response return karenge.
         */
        return buildAuthResponse(
                savedUser,
                "Google authentication successful and user registered"
        );
    }

    /**
     * User account deactivate karega.
     *
     * Hard delete nahi hoga.
     *
     * Saath mein user ke refresh tokens bhi revoke karenge.
     */
    @Override
    public void deactivateUser(UUID userId) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId
                        )
                );

        /*
         * Account deactivate.
         */
        user.setActive(false);

        userRepository.save(user);

        /*
         * User ke existing refresh tokens remove/revoke.
         *
         * Isse deactivated user refresh token use karke
         * new access token nahi le payega.
         */
        tokenService.revokeAllUserTokens(user);
    }

    /**
     * User entity + JWT token information ko
     * AuthResponse mein convert karega.
     */
    private AuthResponse buildAuthResponse(
            User user,
            String message
    ) {

        /*
         * Access token + refresh token generate.
         */
        TokenResponse tokenResponse =
                tokenService.generateTokens(user);

        /*
         * Complete authentication response.
         */
        return AuthResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profileImage(user.getProfileImage())
                .authProvider(user.getAuthProvider())
                .role(user.getRole())
                .active(user.getActive())

                /*
                 * JWT information
                 */
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .tokenType(tokenResponse.getTokenType())
                .expiresIn(tokenResponse.getExpiresIn())

                .message(message)
                .build();
    }
}