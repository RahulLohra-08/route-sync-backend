package com.routesync.backend.security.websocket;

import com.routesync.backend.security.CustomUserDetailsService;
import com.routesync.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class StompAuthChannelInterceptor
        implements ChannelInterceptor {

    private final JwtService jwtService;

    private final CustomUserDetailsService userDetailsService;

    private final WebSocketAuthorizationService
            webSocketAuthorizationService;

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {

        /*
         * Henglish:
         *
         * Existing STOMP accessor ko retrieve kar rahe hain.
         *
         * Authentication ke case mein ye approach important hai
         * because isi message ke headers mein Principal attach hoga.
         */
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) {

            log.warn(
                    "STOMP message received without StompHeaderAccessor"
            );

            return message;
        }

        StompCommand command =
                accessor.getCommand();

        if (command == null) {
            return message;
        }

        // =====================================================
        // CONNECT
        // =====================================================

        if (StompCommand.CONNECT.equals(command)) {

            authenticateConnect(accessor);

            /*
             * Henglish:
             *
             * Authentication CONNECT frame ke headers mein
             * set ho chuki hai.
             *
             * Spring is Principal ko WebSocket/STOMP session ke
             * saath associate karega.
             */
            log.info(
                    "STOMP CONNECT accepted: user={} session={}",
                    accessor.getUser() != null
                            ? accessor.getUser().getName()
                            : "NULL",
                    accessor.getSessionId()
            );

            return message;
        }

        // =====================================================
        // SUBSCRIBE / SEND
        // =====================================================

        if (StompCommand.SUBSCRIBE.equals(command)
                || StompCommand.SEND.equals(command)) {

            Authentication authentication =
                    extractAuthentication(accessor);

            /*
             * IMPORTANT:
             *
             * CONNECT ke baad user yahan available hona chahiye.
             *
             * Agar yahan null hai to iska matlab hai ki
             * authenticated Principal STOMP session mein persist
             * nahi hua.
             */
            if (authentication == null) {

                log.error(
                        "STOMP {} rejected: no authenticated user. session={}, destination={}",
                        command,
                        accessor.getSessionId(),
                        accessor.getDestination()
                );

                throw new MessagingException(
                        "WebSocket authentication required"
                );
            }

            String destination =
                    accessor.getDestination();

            if (destination == null
                    || destination.isBlank()) {

                throw new MessagingException(
                        "STOMP destination is required"
                );
            }

            log.debug(
                    "STOMP {} authenticated: user={} destination={} session={}",
                    command,
                    authentication.getName(),
                    destination,
                    accessor.getSessionId()
            );

            // =================================================
            // SUBSCRIBE AUTHORIZATION
            // =================================================

            if (StompCommand.SUBSCRIBE.equals(command)) {

                webSocketAuthorizationService
                        .authorizeSubscribe(
                                authentication,
                                destination
                        );
            }

            // =================================================
            // SEND AUTHORIZATION
            // =================================================

            if (StompCommand.SEND.equals(command)) {

                webSocketAuthorizationService
                        .authorizeSend(
                                authentication,
                                destination
                        );
            }
        }

        return message;
    }

    // =========================================================
    // CONNECT AUTHENTICATION
    // =========================================================

    private void authenticateConnect(
            StompHeaderAccessor accessor
    ) {

        String token =
                extractToken(accessor);

        if (token == null) {

            log.warn(
                    "STOMP CONNECT rejected: missing Authorization header"
            );

            throw new MessagingException(
                    "Missing Authorization header"
            );
        }

        try {

            /*
             * JWT subject = user ID.
             */
            String userId =
                    jwtService.extractUsername(token);

            log.debug(
                    "STOMP JWT subject extracted: {}",
                    userId
            );

            /*
             * Database se current user load karenge.
             */
            UserDetails userDetails =
                    userDetailsService
                            .loadUserByUsername(userId);

            /*
             * JWT signature + username + expiry validate.
             */
            if (!jwtService.isTokenValid(
                    token,
                    userDetails
            )) {

                throw new IllegalArgumentException(
                        "Expired or invalid JWT"
                );
            }

            /*
             * Spring Security Authentication object.
             */
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            /*
             * =================================================
             * MOST IMPORTANT LINE
             * =================================================
             *
             * Authentication ko STOMP CONNECT message ke
             * Principal ke roop mein attach kar rahe hain.
             *
             * Spring is user ko subsequent STOMP messages
             * (SUBSCRIBE / SEND / etc.) ke saath associate karega.
             */
            accessor.setUser(authentication);

            log.info(
                    "STOMP CONNECT authenticated successfully: user={} roles={} session={}",
                    authentication.getName(),
                    authentication.getAuthorities(),
                    accessor.getSessionId()
            );

        } catch (Exception exception) {

            log.warn(
                    "STOMP authentication failed: {}",
                    exception.getMessage()
            );

            throw new MessagingException(
                    "Invalid or expired JWT",
                    exception
            );
        }
    }

    // =========================================================
    // EXTRACT AUTHENTICATION
    // =========================================================

    private Authentication extractAuthentication(
            StompHeaderAccessor accessor
    ) {

        if (accessor.getUser()
                instanceof Authentication authentication) {

            return authentication;
        }

        return null;
    }

    // =========================================================
    // EXTRACT JWT
    // =========================================================

    private String extractToken(
            StompHeaderAccessor accessor
    ) {

        List<String> headers =
                accessor.getNativeHeader(
                        "Authorization"
                );

        if (headers == null
                || headers.isEmpty()) {

            return null;
        }

        String header =
                headers.get(0);

        if (header == null
                || !header.startsWith("Bearer ")) {

            return null;
        }

        return header.substring(7).trim();
    }
}