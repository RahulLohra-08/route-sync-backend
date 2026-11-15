package com.routesync.backend.service;

/**
 * SMS related operations.
 *
 * Henglish:
 * Ye interface SMS sending ka contract define karta hai.
 *
 * Auth/OTP service ko ye nahi pata hoga ki SMS kis provider
 * ke through send ho raha hai.
 *
 * Example:
 * MSG91 -> SmsServiceImpl
 * Twilio -> SmsServiceImpl
 * AWS SNS -> SmsServiceImpl
 *
 * Future mein provider change karna ho to OTP business logic
 * ko change karne ki zarurat nahi padegi.
 */
public interface SmsService {

    /**
     * User ke mobile number par OTP send karta hai.
     *
     * @param mobileNumber user ka mobile number
     * @param otp generated OTP
     */
    void sendOtp(String mobileNumber, String otp);
}