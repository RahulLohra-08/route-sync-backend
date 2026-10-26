package com.routesync.backend.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Twilio SDK configuration.
 *
 * Henglish:
 * Application start hone ke time Twilio SDK ko initialize karta hai.
 *
 * Important:
 * Yahan actual SMS send nahi hota.
 * Sirf Twilio authentication configuration initialize hoti hai.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class TwilioConfig {

    private final TwilioProperties twilioProperties;

    /**
     * Application startup ke baad Twilio SDK initialize karega.
     */
    @PostConstruct
    public void initializeTwilio() {

        validateConfiguration();

        Twilio.init(
                twilioProperties.getAccountSid().trim(),
                twilioProperties.getAuthToken().trim()
        );

        log.info(
                "Twilio initialized successfully. Account SID: {}",
                maskAccountSid(
                        twilioProperties.getAccountSid()
                )
        );
    }

    /**
     * Required Twilio configuration validate karta hai.
     *
     * Henglish:
     * Credentials missing hone par application startup par hi
     * clear error milega instead of later OTP request ke time.
     */
    private void validateConfiguration() {

        if (isBlank(twilioProperties.getAccountSid())) {
            throw new IllegalStateException(
                    "TWILIO_ACCOUNT_SID is not configured"
            );
        }

        if (isBlank(twilioProperties.getAuthToken())) {
            throw new IllegalStateException(
                    "TWILIO_AUTH_TOKEN is not configured"
            );
        }

        if (isBlank(twilioProperties.getFromNumber())) {
            throw new IllegalStateException(
                    "TWILIO_FROM_NUMBER is not configured"
            );
        }
    }

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }

    /**
     * Account SID ko safely log karta hai.
     *
     * Henglish:
     * Complete credential kabhi logs mein nahi dikhayenge.
     */
    private String maskAccountSid(String value) {

        if (value == null || value.length() < 8) {
            return "INVALID";
        }

        return value.substring(0, 4)
                + "********"
                + value.substring(value.length() - 4);
    }
}