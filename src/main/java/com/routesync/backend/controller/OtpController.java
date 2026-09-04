package com.routesync.backend.controller;

import com.routesync.backend.dto.otp.GenerateOtpRequest;
import com.routesync.backend.dto.otp.OtpResponse;
import com.routesync.backend.dto.otp.VerifyOtpRequest;
import com.routesync.backend.service.OtpService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OTP related REST APIs ko handle karta hai.
 */
@RestController
@RequestMapping("/api/v1/auth/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;


    /**
     * Mobile number par OTP send karega.
     *
     * POST /api/v1/auth/otp/send
     */
    @PostMapping("/send")
    public ResponseEntity<OtpResponse> generateOtp(
            @Valid @RequestBody GenerateOtpRequest request
    ) {

        String otp = otpService.generateOtp(
                request.getPhoneNumber(),
                request.getPurpose()
        );


        /*
         * DEVELOPMENT ONLY.
         *
         * Production mein OTP response mein nahi bhejna hai.
         */
        OtpResponse response = OtpResponse.builder()
                .message("OTP sent successfully")
                .otp(otp)
                .build();


        return ResponseEntity.ok(response);
    }


    /**
     * OTP verify karega.
     *
     * NOTE:
     * Actual login flow mein AuthController ka
     * /otp/login endpoint use hoga.
     *
     * Ye endpoint currently OTP service testing ke
     * liye useful hai.
     *
     * POST /api/v1/auth/otp/verify
     */
//    @PostMapping("/verify")
//    public ResponseEntity<OtpResponse> verifyOtp(
//            @Valid @RequestBody VerifyOtpRequest request
//    ) {
//
//        otpService.verifyOtp(
//                request.getPhoneNumber(),
//                request.getOtp(),
//                request.getPurpose()
//        );
//
//
//        OtpResponse response = OtpResponse.builder()
//                .message("OTP verified successfully")
//                .otp(null)
//                .build();
//
//
//        return ResponseEntity.ok(response);
//    }
}