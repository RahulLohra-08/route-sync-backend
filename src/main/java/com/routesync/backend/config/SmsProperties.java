package com.routesync.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SMS provider related configuration.
 *
 * Henglish:
 * Is class ka kaam application.properties se SMS ki
 * configuration values read karna hai.
 *
 * Example:
 *
 * routesync.sms.provider
 * routesync.sms.msg91.auth-key
 * routesync.sms.msg91.sender-id
 * routesync.sms.msg91.template-id
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "routesync.sms")
public class SmsProperties {

    /**
     * SMS provider ka naam.
     *
     * Example:
     * msg91
     */
    private String provider;

    /**
     * MSG91 related configuration.
     */
    private Msg91 msg91 = new Msg91();

    /**
     * Country code.
     *
     * India = 91
     */
    private String countryCode;

    /**
     * MSG91 configuration.
     */
    @Getter
    @Setter
    public static class Msg91 {

        /**
         * MSG91 authentication/API key.
         *
         * Henglish:
         * Isko kabhi source code mein hard-code nahi karna.
         */
        private String authKey;

        /**
         * Approved sender ID.
         */
        private String senderId;

        /**
         * MSG91 approved OTP template ID.
         */
        private String templateId;
    }
}