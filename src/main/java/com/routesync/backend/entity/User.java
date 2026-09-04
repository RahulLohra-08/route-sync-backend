package com.routesync.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User entity database ke "users" table ko represent karti hai.
 *
 * RouteSync mein ek User:
 * - Passenger ho sakta hai
 * - Driver ho sakta hai
 * - Admin ho sakta hai
 *
 * Login ke liye Google ya OTP use kar sakta hai.
 */
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_phone", columnList = "phone_number"),
                @Index(name = "idx_user_google_id", columnList = "google_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * User ka unique UUID.
     *
     * Integer/Long ID ki jagah UUID use kar rahe hain
     * kyunki distributed system aur future microservices
     * ke liye UUID better choice hai.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    /**
     * User ka full name.
     *
     * Google login ke case mein Google profile se aa sakta hai.
     * OTP login ke case mein user registration ke time provide karega.
     */
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;


    // Profile image ka URL
    private String profileImage;

    /**
     * Email address.
     *
     * Google login mein ye Google account se milega.
     * OTP login mein optional ho sakta hai.
     */
    @Column(
            name = "email",
            unique = true,
            length = 150
    )
    private String email;


    /**
     * Mobile number.
     *
     * OTP login ka primary identifier ye hoga.
     */
    @Column(
            name = "phone_number",
            unique = true,
            length = 20
    )
    private String phoneNumber;


    /**
     * Google ka unique user identifier.
     *
     * IMPORTANT:
     * Hum Google email ko primary identity nahi maanenge.
     * Google ka "sub" / unique ID future OAuth implementation
     * mein yahan store karenge.
     */
    @Column(
            name = "google_id",
            unique = true,
            length = 255
    )
    private String googleId;


    /**
     * User ka authentication provider.
     *
     * Example:
     * GOOGLE -> Google login
     * OTP    -> Mobile OTP login
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "auth_provider",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.OTP;


    /**
     * User ka role.
     *
     * Default role PASSENGER rakhenge.
     *
     * New user normally passenger hoga.
     * Admin/Driver role ko controlled process se assign karenge.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private UserRole role = UserRole.PASSENGER;


    /**
     * Account active hai ya nahi.
     *
     * Admin future mein kisi user ko deactivate kar sakta hai.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = true;


    /**
     * User kab create hua.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    /**
     * User ki last information update kab hui.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    /**
     * Entity database mein insert hone se pehle automatically
     * creation aur update time set karegi.
     */
    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }


    /**
     * Entity update hone par updatedAt automatically change hoga.
     */
    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = LocalDateTime.now();
    }

}