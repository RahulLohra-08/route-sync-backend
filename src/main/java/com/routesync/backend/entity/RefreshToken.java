package com.routesync.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RefreshToken entity database ke refresh_tokens table ko represent karti hai.
 *
 * Refresh token ko database mein store karenge taaki:
 * - Logout par token revoke kar sakein
 * - Expired token identify kar sakein
 * - Token ko invalidate kar sakein
 * - Future mein token rotation implement kar sakein
 */
@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_token_token", columnList = "token"),
                @Index(name = "idx_refresh_token_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    /**
     * Refresh token ka unique database ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Actual refresh token value.
     *
     * Isko unique rakhenge taaki same token multiple records mein
     * exist na kare.
     */
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    /**
     * Token kis user ka hai.
     *
     * User entity ke saath ManyToOne relationship.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    /**
     * Refresh token kab expire hoga.
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /**
     * Token revoke hua hai ya nahi.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    /**
     * Token create hone ka time.
     */
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Check karta hai ki refresh token expired hai ya nahi.
     */
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }

    /**
     * Check karta hai ki token use karne ke liye valid hai ya nahi.
     */
    public boolean isValid() {
        return !isExpired() && !Boolean.TRUE.equals(revoked);
    }
}