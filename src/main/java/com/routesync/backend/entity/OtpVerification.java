package com.routesync.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Database mein OTP verification information store karta hai.
 */
@Entity
@Table(
        name = "otp_verifications",
        indexes = {
                @Index(
                        name = "idx_otp_phone_purpose",
                        columnList = "phone_number,purpose"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * OTP kis user ke liye hai.
     *
     * New user ke case mein user abhi exist nahi karega,
     * isliye nullable rakha gaya hai.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    /**
     * OTP jis mobile number ke liye generate hua.
     *
     * Existing OTP records migrate hone tak nullable rakha gaya hai.
     */
    @Column(name = "phone_number", nullable = false, length = 10)
    private String phoneNumber;

    /**
     * 6 digit OTP.
     */
    @Column(nullable = false, length = 6)
    private String otp;

    /**
     * OTP purpose.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpPurpose purpose;

    /**
     * OTP kab expire hoga.
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * OTP verify hua ya nahi.
     */
    @Column(nullable = false)
    private boolean verified;

    /**
     * Verification attempts.
     */
    @Column(nullable = false)
    private int attempts;

    /**
     * OTP create hone ka time.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Entity save hone se pehle creation time set karega.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}