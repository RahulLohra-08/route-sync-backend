package com.routesync.backend.service.impl;

import com.routesync.backend.dto.auth.AuthResponse;
import com.routesync.backend.dto.auth.TokenResponse;
import com.routesync.backend.entity.*;
import com.routesync.backend.exception.BadRequestException;
import com.routesync.backend.repository.OtpRepository;
import com.routesync.backend.repository.UserRepository;
import com.routesync.backend.security.CustomUserDetailsService;
import com.routesync.backend.security.JwtService;
import com.routesync.backend.service.EmailService;
import com.routesync.backend.service.OtpService;
import com.routesync.backend.service.SmsService;

import com.routesync.backend.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * OTP service ki actual business logic.
 *
 * Supports:
 * - Mobile OTP
 * - Email OTP
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;

    private final UserRepository userRepository;

    /**
     * Existing SMS service.
     */
    private final SmsService smsService;

    private final JwtService jwtService;
    private final TokenService tokenService;

    private final CustomUserDetailsService userDetailsService;

    /**
     * Email OTP ke liye email service.
     */
    private final EmailService emailService;

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


    // =========================================================
    // MOBILE OTP
    // =========================================================

    /**
     * Mobile number ke liye OTP generate karega.
     *
     * @return generated OTP
     */
    @Override
    public String sendOtp(
            String phoneNumber,
            OtpPurpose purpose
    ) {

        /*
         * Mobile number ko standard format mein convert kar rahe hain.
         *
         * Example:
         *
         * 8340357997
         *      ↓
         * +918340357997
         */
        String normalizedNumber =
                normalizeIndianMobileNumber(phoneNumber);

        /*
         * Agar existing user hai to user object mil jayega.
         *
         * New user ke case mein null ho sakta hai.
         */
        User user = userRepository
                .findByPhoneNumber(normalizedNumber)
                .orElse(null);

        /*
         * 6 digit OTP generate kar rahe hain.
         */
        String otp = generateOtp();

        /*
         * OTP entity create kar rahe hain.
         */
        OtpVerification otpVerification =
                OtpVerification.builder()
                        .user(user)
                        .phoneNumber(normalizedNumber)
                        .email(null)
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
         * Database mein save hone ke baad SMS send.
         */
        smsService.sendOtp(
                normalizedNumber,
                otp
        );

        /*
         * DEVELOPMENT ONLY
         *
         * Production mein OTP response mein
         * return nahi karna hai.
         */
        return otp;
    }


    // =========================================================
    // EMAIL OTP
    // =========================================================

    /**
     * Email address ke liye OTP generate aur send karega.
     *
     * Example:
     *
     * rahul@gmail.com
     *       ↓
     * rahul@gmail.com
     */
    @Override
    public void sendEmailOtp(
            String email,
            OtpPurpose purpose
    ) {

        /*
         * Email ko standard format mein convert kar rahe hain.
         */
        String normalizedEmail =
                normalizeEmail(email);

        log.info(
                "Generating Email OTP for: {}",
                normalizedEmail
        );

        /*
         * Existing user find karo.
         *
         * Agar user exist nahi karta,
         * to user null rahega.
         */
        User user = userRepository
                .findByEmail(normalizedEmail)
                .orElse(null);

        /*
         * 6 digit OTP generate.
         */
        String otp = generateOtp();

        /*
         * OTP entity create.
         *
         * Email OTP mein:
         *
         * phoneNumber = null
         * email       = normalizedEmail
         */
        OtpVerification otpVerification =
                OtpVerification.builder()
                        .user(user)
                        .phoneNumber(null)
                        .email(normalizedEmail)
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
         * Database mein OTP save.
         */
        otpRepository.save(otpVerification);

        /*
         * Email ke through OTP send.
         */
        emailService.sendOtp(
                normalizedEmail,
                otp
        );

        log.info(
                "Email OTP sent successfully to: {}",
                normalizedEmail
        );
    }


    // =========================================================
    // MOBILE OTP VERIFICATION
    // =========================================================

    /**
     * Mobile OTP verify karega.
     */
    @Override
    public void verifyOtp(
            String phoneNumber,
            String otp,
            OtpPurpose purpose
    ) {

        /*
         * Phone number ko same format mein convert kar rahe hain
         * jaisa sendOtp() mein kiya tha.
         */
        String normalizedNumber =
                normalizeIndianMobileNumber(phoneNumber);

        /*
         * Latest UNUSED OTP find kar rahe hain.
         *
         * verified = false
         * aur latest created OTP.
         */
        OtpVerification otpVerification =
                otpRepository
                        .findTopByPhoneNumberAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
                                normalizedNumber,
                                purpose
                        )
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "No active OTP found for this mobile number"
                                )
                        );

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

        log.info(
                "Mobile OTP verified successfully for: {}",
                normalizedNumber
        );
    }


    // =========================================================
    // EMAIL OTP VERIFICATION
    // =========================================================

    /**
     * Email OTP verify karega.
     *
     * @return
     */
    @Override
    public AuthResponse verifyEmailOtp(
            String email,
            String otp,
            OtpPurpose purpose
    ) {

        String normalizedEmail = normalizeEmail(email);

        /*
         * STEP 1
         *
         * Latest unverified OTP find karenge.
         */
        OtpVerification otpVerification =
                otpRepository
                        .findTopByEmailAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
                                normalizedEmail,
                                purpose
                        )
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "No active OTP found for this email"
                                )
                        );

        /*
         * STEP 2
         *
         * Maximum verification attempts check.
         */
        if (otpVerification.getAttempts() >= MAX_ATTEMPTS) {

            throw new BadRequestException(
                    "Maximum OTP verification attempts exceeded"
            );
        }

        /*
         * Attempt count increase.
         *
         * Wrong OTP hone par bhi attempt count increase hoga.
         */
        otpVerification.setAttempts(
                otpVerification.getAttempts() + 1
        );

        /*
         * STEP 3
         *
         * OTP expiry check.
         */
        if (!LocalDateTime.now().isBefore(
                otpVerification.getExpiresAt()
        )) {

            otpRepository.save(otpVerification);

            throw new BadRequestException(
                    "OTP has expired"
            );
        }

        /*
         * STEP 4
         *
         * OTP match check.
         */
        if (!otpVerification.getOtp().equals(otp)) {

            otpRepository.save(otpVerification);

            throw new BadRequestException(
                    "Invalid OTP"
            );
        }

        /*
         * STEP 5
         *
         * OTP successfully verified.
         */
        otpVerification.setVerified(true);

        /*
         * STEP 6
         *
         * Email ke basis par existing user find karenge.
         *
         * Important:
         * OTP record ka user null ho sakta hai.
         * Isliye directly:
         *
         * otpVerification.getUser()
         *
         * par depend nahi karenge.
         */
        User user = userRepository
                .findByEmail(normalizedEmail)
                .orElse(null);

        /*
         * STEP 7
         *
         * Existing user.
         */
        if (user != null) {

            /*
             * Deactivated user login nahi kar sakta.
             */
            if (!Boolean.TRUE.equals(user.getActive())) {

                throw new BadRequestException(
                        "User account is deactivated"
                );
            }

            /*
             * OTP record ko user ke saath associate kar dete hain.
             */
            otpVerification.setUser(user);

            otpRepository.save(otpVerification);

            /*
             * Existing authentication architecture
             * ke through JWT generate karenge.
             */
            TokenResponse tokenResponse =
                    tokenService.generateTokens(user);

            log.info(
                    "Email OTP login successful for: {}",
                    normalizedEmail
            );

            return buildAuthResponse(
                    user,
                    tokenResponse,
                    "Email OTP login successful"
            );
        }

        /*
         * STEP 8
         *
         * New user.
         *
         * Public email OTP registration se
         * sirf PASSENGER create hoga.
         *
         * Driver/Admin role user choose nahi kar sakta.
         */
        User newUser = User.builder()
                .fullName(extractNameFromEmail(normalizedEmail))
                .email(normalizedEmail)
                .phoneNumber(null)
                .authProvider(AuthProvider.OTP)
                .role(UserRole.PASSENGER)
                .active(true)
                .build();

        /*
         * STEP 9
         *
         * New user save.
         */
        User savedUser = userRepository.save(newUser);

        /*
         * STEP 10
         *
         * OTP record ko newly created user se associate.
         */
        otpVerification.setUser(savedUser);

        otpRepository.save(otpVerification);

        /*
         * STEP 11
         *
         * Existing TokenService se JWT generate.
         */
        TokenResponse tokenResponse =
                tokenService.generateTokens(savedUser);

        log.info(
                "New PASSENGER created through Email OTP: {}",
                normalizedEmail
        );

        /*
         * STEP 12
         *
         * Authentication response.
         */
        return buildAuthResponse(
                savedUser,
                tokenResponse,
                "Email OTP verified and account created successfully"
        );
    }

    private String extractNameFromEmail(String email) {

        String username = email.substring(
                0,
                email.indexOf('@')
        );

        return username.length() > 100
                ? username.substring(0, 100)
                : username;
    }
    private AuthResponse buildAuthResponse(
            User user,
            TokenResponse tokenResponse,
            String message
    ) {

        return AuthResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profileImage(user.getProfileImage())
                .authProvider(user.getAuthProvider())
                .role(user.getRole())
                .active(user.getActive())

                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .tokenType(tokenResponse.getTokenType())
                .expiresIn(tokenResponse.getExpiresIn())

                .message(message)
                .build();
    }

    // =========================================================
    // OTP GENERATOR
    // =========================================================

    /**
     * Secure 6 digit OTP generate karta hai.
     */
    private String generateOtp() {

        return String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );
    }


    // =========================================================
    // MOBILE NUMBER NORMALIZATION
    // =========================================================

    /**
     * Indian mobile number ko E.164 format mein convert karta hai.
     *
     * Examples:
     *
     * 8340357997
     *      ↓
     * +918340357997
     *
     * +918340357997
     *      ↓
     * +918340357997
     */
    private String normalizeIndianMobileNumber(
            String mobileNumber
    ) {

        if (mobileNumber == null) {

            throw new BadRequestException(
                    "Mobile number cannot be null"
            );
        }

        mobileNumber =
                mobileNumber.trim();

        /*
         * Already +91 format.
         */
        if (mobileNumber.matches(
                "\\+91\\d{10}"
        )) {

            return mobileNumber;
        }

        /*
         * 10 digit Indian number.
         */
        if (mobileNumber.matches(
                "\\d{10}"
        )) {

            return "+91" + mobileNumber;
        }

        throw new BadRequestException(
                "Invalid Indian mobile number"
        );
    }


    // =========================================================
    // EMAIL NORMALIZATION
    // =========================================================

    /**
     * Email ko consistent format mein convert karta hai.
     *
     * Example:
     *
     * Rahul@Gmail.com
     *      ↓
     * rahul@gmail.com
     */
    private String normalizeEmail(
            String email
    ) {

        if (email == null ||
                email.isBlank()) {

            throw new BadRequestException(
                    "Email cannot be empty"
            );
        }

        return email
                .trim()
                .toLowerCase();
    }
}