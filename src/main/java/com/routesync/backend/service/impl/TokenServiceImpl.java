package com.routesync.backend.service.impl;

import com.routesync.backend.dto.auth.TokenResponse;
import com.routesync.backend.entity.RefreshToken;
import com.routesync.backend.entity.User;
import com.routesync.backend.repository.RefreshTokenRepository;
import com.routesync.backend.security.JwtService;
import com.routesync.backend.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import com.routesync.backend.exception.BadRequestException;

/**
 * TokenService ka actual implementation.
 *
 * Is class mein:
 * - Access token generate hoga
 * - Refresh token generate hoga
 * - Refresh token DB mein save hoga
 * - Refresh token validate hoga
 * - Token revoke hoga
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TokenServiceImpl implements TokenService {

    private final JwtService jwtService;

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * User ke liye access + refresh token generate karta hai.
     */
    @Override
    public TokenResponse generateTokens(User user) {

        /*
         * Spring Security ke UserDetails object ko create kar rahe hain.
         *
         * JWT subject mein User UUID jayega.
         */

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getId().toString())
                        .password("")
                        .authorities(
                                "ROLE_" + user.getRole().name()
                        )
                        .build();

        /*
         * Access token generate.
         */
        String accessToken = jwtService.generateAccessToken(userDetails);

        /*
         * Refresh token generate.
         */
        String refreshTokenValue = jwtService.generateRefreshToken(userDetails);

        /*
         * Refresh token ko database mein store karenge.
         */
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(
                        LocalDateTime.now()
                                .plusDays(7)
                )
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(
                        jwtService.getAccessTokenExpiration()
                )
                .build();
    }

    /**
     * Refresh token ke through new access token generate karta hai.
     */
    @Override
    @Transactional
    public TokenResponse refreshAccessToken(
            String refreshTokenValue
    ) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(refreshTokenValue)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Invalid refresh token"
                                )
                        );

        /*
         * Expired ya revoked token allow nahi karenge.
         */
        if (!refreshToken.isValid()) {

            throw new BadRequestException(
                    "Refresh token is expired or revoked"
            );
        }

        User user = refreshToken.getUser();

        /*
         * User active hona chahiye.
         */
        if (!user.getActive()) {

            throw new BadRequestException(
                    "User account is inactive"
            );
        }

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getId().toString())
                        .password("")
                        .authorities(
                                "ROLE_" + user.getRole().name()
                        )
                        .build();

        /*
         * New access token generate.
         */
        String newAccessToken =
                jwtService.generateAccessToken(userDetails);

        /*
         * Abhi refresh token same rahega.
         *
         * Future mein refresh token rotation implement kar sakte hain.
         */
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(
                        jwtService.getAccessTokenExpiration()
                )
                .build();
    }

    /**
     * Specific refresh token revoke karta hai.
     */
    @Override
    public void revokeRefreshToken(
            String refreshTokenValue
    ) {

        refreshTokenRepository
                .findByToken(refreshTokenValue)
                .ifPresent(refreshToken -> {

                    refreshToken.setRevoked(true);

                    refreshTokenRepository.save(refreshToken);
                });
    }

    /**
     * User ke saare refresh tokens revoke karta hai.
     */
    @Override
    public void revokeAllUserTokens(User user) {

        refreshTokenRepository.deleteByUser(user);
    }
}