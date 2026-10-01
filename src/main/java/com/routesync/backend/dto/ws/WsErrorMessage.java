package com.routesync.backend.dto.ws;

import java.time.LocalDateTime;

/**
 * Structured error payload delivered to a single STOMP session over its
 * private "/user/queue/errors" destination when a WebSocket-originated
 * action (e.g. a driver's GPS push) fails.
 *
 * Kept deliberately close to the shape of {@code ErrorResponse} (the
 * REST error DTO) so both transports feel consistent to a client app.
 */
public record WsErrorMessage(
        String code,
        String message,
        String destination,
        LocalDateTime timestamp
) {

    public static WsErrorMessage of(String code, String message, String destination) {
        return new WsErrorMessage(code, message, destination, LocalDateTime.now());
    }
}
