package com.routesync.backend.repository;

import com.routesync.backend.entity.OtpPurpose;
import com.routesync.backend.entity.OtpVerification;
import com.routesync.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * OtpVerification entity ke database operations handle karta hai.
 *
 * JpaRepository ki wajah se basic CRUD operations
 * automatically available ho jaate hain.
 */
public interface OtpRepository extends JpaRepository<OtpVerification, UUID> {

    /**
     * User + OTP + Purpose ke basis par OTP find karega.
     *
     * Example:
     * User = Rahul
     * OTP = 482913
     * Purpose = LOGIN
     */
    Optional<OtpVerification> findByUserAndOtpAndPurpose(
            User user,
            String otp,
            OtpPurpose purpose
    );

    /**
     * Mobile number aur purpose ke basis par latest OTP find karega.
     */
    Optional<OtpVerification>
    findTopByPhoneNumberAndPurposeOrderByCreatedAtDesc(
            String phoneNumber,
            OtpPurpose purpose
    );
}