package com.routesync.backend.service.impl;

import com.routesync.backend.config.TwilioProperties;
import com.routesync.backend.service.SmsService;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Twilio based SMS service implementation.
 *
 * Henglish:
 * Is class ka responsibility sirf SMS send karna hai.
 *
 * Twilio SDK initialization TwilioConfig handle karega.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmsServiceImpl implements SmsService {

    private final TwilioProperties twilioProperties;

    @Override
    public void sendOtp(
            String mobileNumber,
            String otp
    ) {

//        sms_2fa
//        String messageBody = "RouteSync OTP: " + otp + ". This OTP is valid for 5 minutes.";
        String messageBody = "sms_2fa" + " RouteSync OTP: " + otp + ". This OTP is valid for 5 minutes.";

        try {

            Message message = Message.creator(
                    new PhoneNumber(mobileNumber),
                    new PhoneNumber(twilioProperties.getFromNumber()),
                    "sms_2fa"
            ).create();

            log.info(
                    "OTP SMS sent successfully. Message SID: {}",
                    message.getSid()
            );

        } catch (ApiException exception) {

            log.error(
                    "Twilio SMS failed. HTTP status: {}, Error code: {}, Message: {}",
                    exception.getStatusCode(),
                    exception.getCode(),
                    exception.getMessage()
            );

            throw new IllegalStateException(
                    "Unable to send OTP SMS. Please try again later."
            );
        }
    }
}