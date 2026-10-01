package com.routesync.backend.security.websocket;

import com.routesync.backend.security.CustomUserDetailsService;
import com.routesync.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Authenticates every STOMP CONNECT frame using the same JWT that the
 * REST API already trusts.
 *
 * WHY THIS EXISTS
 * ----------------
 * SecurityConfig permits "/ws/**" at the HTTP layer because the raw
 * WebSocket handshake (the initial HTTP Upgrade request) is not where
 * STOMP clients attach the JWT - @stomp/stompjs (and every other STOMP
 * client) sends custom headers as part of the STOMP CONNECT *frame*,
 * not as HTTP headers on the handshake. So authentication for this
 * transport has to happen here, at the STOMP frame level, not in
 * JwtAuthenticationFilter.
 *
 * Without this interceptor, ANY client that can reach /ws could
 * subscribe to /topic/trips/{id}/location for any trip, and (once the
 * WS ingestion endpoint is added) could even push fake GPS points.
 *
 * HOW A CLIENT AUTHENTICATES
 * ---------------------------
 * The client must send the access token as a native STOMP header on
 * the CONNECT frame:
 *
 *     Authorization: Bearer <jwt>
 *
 * On success we attach a Spring Security Authentication as the STOMP
 * session's Principal, exactly mirroring what JwtAuthenticationFilter
 * does for REST calls (same username = user UUID, same
 * ROLE_* authorities). Every later frame on this session (SUBSCRIBE,
 * SEND, DISCONNECT) carries this Principal, so
 * @MessageMapping methods can call principal.getName() and
 * @PreAuthorize can evaluate hasRole(...) exactly like a REST controller.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticateConnect(accessor);
        }

        // CONNECT already rejected bad tokens, so by the time a
        // SUBSCRIBE/SEND frame arrives on this session it already has
        // a Principal attached by Spring's session-scoped header
        // propagation. We defensively re-check here in case some
        // client library reuses a channel without a proper CONNECT.
        if ((StompCommand.SUBSCRIBE.equals(accessor.getCommand())
                || StompCommand.SEND.equals(accessor.getCommand()))
                && accessor.getUser() == null) {

            log.warn(
                    "Rejected {} with no authenticated principal on session {}",
                    accessor.getCommand(),
                    accessor.getSessionId()
            );

            throw new org.springframework.messaging.MessagingException(
                    "Not authenticated. Connect with a valid JWT first."
            );
        }

        return message;
    }

    private void authenticateConnect(StompHeaderAccessor accessor) {

        String token = extractToken(accessor);

        if (token == null) {
            log.warn(
                    "STOMP CONNECT rejected: missing Authorization header (session {})",
                    accessor.getSessionId()
            );

            throw new org.springframework.messaging.MessagingException(
                    "Missing Authorization header on STOMP CONNECT"
            );
        }

        try {
            String userId = jwtService.extractUsername(token);

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(userId);

            if (!jwtService.isTokenValid(token, userDetails)) {
                throw new IllegalArgumentException("Token expired or invalid");
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            // This is what makes accessor.getUser() / Principal available
            // to every subsequent frame on this WebSocket session.
            accessor.setUser(authentication);

            log.info(
                    "STOMP CONNECT authenticated: user={} authorities={} session={}",
                    userDetails.getUsername(),
                    userDetails.getAuthorities(),
                    accessor.getSessionId()
            );

        } catch (Exception exception) {

            log.warn(
                    "STOMP CONNECT rejected: invalid token on session {} ({})",
                    accessor.getSessionId(),
                    exception.getMessage()
            );

            throw new org.springframework.messaging.MessagingException(
                    "Invalid or expired token", exception
            );
        }
    }

    /**
     * Reads the token from the "Authorization" STOMP header, supporting
     * the same "Bearer &lt;token&gt;" format used by the REST API.
     */
    private String extractToken(StompHeaderAccessor accessor) {

        List<String> authHeaders =
                accessor.getNativeHeader("Authorization");

        if (authHeaders == null || authHeaders.isEmpty()) {
            return null;
        }

        String header = authHeaders.get(0);

        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }

        return header.substring(7);
    }
}
