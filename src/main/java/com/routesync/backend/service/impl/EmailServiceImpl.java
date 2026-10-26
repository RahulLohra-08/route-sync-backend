package com.routesync.backend.service.impl;

import com.routesync.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtp(
            String email,
            String otp
    ) {

        String subject = "RouteSync Login OTP";

        String message =
                "Hello,\n\n" +
                        "Your RouteSync login OTP is: " + otp + "\n\n" +
                        "This OTP is valid for 5 minutes.\n" +
                        "Do not share this OTP with anyone.\n\n" +
                        "Regards,\n" +
                        "RouteSync Team";

        try {

            SimpleMailMessage mail = new SimpleMailMessage();

            mail.setTo(email);
            mail.setSubject(subject);
            mail.setText(message);

            mailSender.send(mail);

            log.info(
                    "OTP email sent successfully to: {}",
                    email
            );

        } catch (Exception e) {

            log.error(
                    "Failed to send OTP email to: {}",
                    email,
                    e
            );

            throw new RuntimeException(
                    "Failed to send OTP email"
            );
        }
    }
}