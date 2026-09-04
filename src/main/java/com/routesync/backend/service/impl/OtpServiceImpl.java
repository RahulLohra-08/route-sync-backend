package com.routesync.backend.service.impl;

import com.routesync.backend.entity.OtpPurpose;
import com.routesync.backend.entity.OtpVerification;
import com.routesync.backend.entity.User;
import com.routesync.backend.exception.BadRequestException;
import com.routesync.backend.repository.OtpRepository;
import com.routesync.backend.repository.UserRepository;
import com.routesync.backend.service.OtpService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import java.util.Optional;

/**
 * OTP service ki actual business logic.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;

    private final UserRepository userRepository;

    /**
     * Security ke liye SecureRandom use kar rahe hain.
     */
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * OTP 5 minutes ke liye valid rahega.
     */
    private static final int OTP_EXPIRY_MINUTES = 5;

    /**
     * Maximum verification attempts.
     */
    private static final int MAX_ATTEMPTS = 5;


    /**
     * Mobile number ke liye OTP generate karega.
     */
    @Override
    public String generateOtp(
            String phoneNumber,
            OtpPurpose purpose
    ) {

        /*
         * Agar existing user hai to user object mil jayega.
         *
         * New user ke case mein null ho sakta hai.
         */
        User user = userRepository
                .findByPhoneNumber(phoneNumber)
                .orElse(null);


        /*
         * 6 digit OTP generate kar rahe hain.
         */
        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );


        /*
         * OTP entity create kar rahe hain.
         *
         * user null ho sakta hai because new user
         * abhi database mein exist nahi karta.
         */
        OtpVerification otpVerification =
                OtpVerification.builder()
                        .user(user)
                        .phoneNumber(phoneNumber)
                        .otp(otp)
                        .purpose(purpose)
                        .expiresAt(
                                LocalDateTime.now()
                                        .plusMinutes(
                                                OTP_EXPIRY_MINUTES
                                        )
                        )
                        .verified(false)
                        .attempts(0)
                        .build();


        /*
         * Database mein OTP save kar rahe hain.
         */
        otpRepository.save(otpVerification);


        /*
         * DEVELOPMENT ONLY
         *
         * Production mein OTP response mein return nahi karna hai.
         * SMS provider ke through send karna hai.
         */
        return otp;
    }


    /**
     * OTP verify karega.
     */

    @Override
    public void verifyOtp(
            String phoneNumber,
            String otp,
            OtpPurpose purpose
    ) {

        /*
         * Latest OTP find kar rahe hain.
         */
        OtpVerification otpVerification =
                otpRepository
                        .findTopByPhoneNumberAndPurposeOrderByCreatedAtDesc(
                                phoneNumber,
                                purpose
                        )
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "No OTP found for this mobile number"
                                )
                        );


        /*
         * OTP already use ho chuka hai.
         */
        if (otpVerification.isVerified()) {

            throw new BadRequestException(
                    "OTP has already been used"
            );
        }


        /*
         * Maximum attempts check.
         */
        if (otpVerification.getAttempts()
                >= MAX_ATTEMPTS) {

            throw new BadRequestException(
                    "Maximum OTP verification attempts exceeded"
            );
        }


        /*
         * Attempt increase kar rahe hain.
         */
        otpVerification.setAttempts(
                otpVerification.getAttempts() + 1
        );


        /*
         * OTP expiry check.
         *
         * Exact expiry time par bhi OTP expired maana jayega.
         */
        if (!LocalDateTime.now()
                .isBefore(otpVerification.getExpiresAt())) {

            otpRepository.save(otpVerification);

            throw new BadRequestException(
                    "OTP has expired"
            );
        }


        /*
         * OTP match check.
         */
        if (!otpVerification.getOtp().equals(otp)) {

            otpRepository.save(otpVerification);

            throw new BadRequestException(
                    "Invalid OTP"
            );
        }


        /*
         * OTP successfully verified.
         */
        otpVerification.setVerified(true);

        otpRepository.save(otpVerification);
    }
}