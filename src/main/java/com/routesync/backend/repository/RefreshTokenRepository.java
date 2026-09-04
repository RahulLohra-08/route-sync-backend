package com.routesync.backend.repository;

import com.routesync.backend.entity.RefreshToken;
import com.routesync.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * RefreshToken ke database operations handle karta hai.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Token value ke basis par refresh token find karta hai.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * User ke saare refresh tokens delete karta hai.
     *
     * Logout-all-devices functionality ke liye useful hoga.
     */
    void deleteByUser(User user);

    /**
     * User ke saare refresh tokens revoke karne ke liye useful.
     */
    long deleteByExpiryDateBefore(
            java.time.LocalDateTime dateTime
    );
}