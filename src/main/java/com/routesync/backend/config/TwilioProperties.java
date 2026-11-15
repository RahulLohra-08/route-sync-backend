package com.routesync.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Twilio configuration.
 *
 * Henglish:
 * Ye class application.properties se Twilio ki
 * configuration values read karegi.
 *
 * Isse Twilio credentials directly service class
 * mein hard-code karne ki zarurat nahi padegi.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "routesync.twilio")
public class TwilioProperties {

    /**
     * Twilio Account SID.
     */
    private String accountSid;

    /**
     * Twilio Authentication Token.
     */
    private String authToken;

    /**
     * Twilio ka verified/owned phone number.
     *
     * Example:
     * +1234567890
     */
    private String fromNumber;
}