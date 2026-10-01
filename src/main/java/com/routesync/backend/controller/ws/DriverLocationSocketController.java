package com.routesync.backend.controller.ws;

import com.routesync.backend.dto.trip.location.UpdateTripLocationRequest;
import com.routesync.backend.dto.ws.WsErrorMessage;
import com.routesync.backend.exception.BadRequestException;
import com.routesync.backend.exception.ResourceNotFoundException;
import com.routesync.backend.service.TripLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

/**
 * WebSocket/STOMP counterpart of {@link com.routesync.backend.controller.driver.DriverLocationController}.
 *
 * TRANSPORT CHOICE - REST vs STOMP for GPS ingestion
 * ----------------------------------------------------
 * The REST endpoint (PATCH /api/v1/driver/trips/{tripId}/location) is kept
 * fully intact and is the recommended *primary* path for the driver app,
 * because HTTP requests are easy to retry individually if one fails on a
 * flaky mobile connection, and they don't depend on a long-lived socket
 * surviving in the background.
 *
 * This STOMP endpoint is an additional, lower-latency path for when the
 * driver app already holds an open WebSocket session (e.g. because it is
 * also displaying its own trip in real time): pushing the point over the
 * existing socket avoids a second TCP/TLS handshake per update and shaves
 * a bit of latency off every broadcast. The mobile client in this project
 * uses it as the fast path and automatically falls back to the REST call
 * if the socket is not connected - see useDriverLocationBroadcaster.ts.
 *
 * Both paths funnel through the exact same TripLocationService, so
 * validation, persistence, and the /topic broadcast behave identically
 * regardless of which transport the driver used.
 *
 * Destination: /app/trips/{tripId}/location  (client SENDs here)
 * Errors:      /user/queue/errors            (client SUBSCRIBEs here)
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class DriverLocationSocketController {

    private final TripLocationService tripLocationService;

    @MessageMapping("/trips/{tripId}/location")
    public void updateLocation(
            @DestinationVariable UUID tripId,
            @Valid @Payload UpdateTripLocationRequest request,
            Principal principal
    ) {

        // principal is guaranteed non-null here: StompAuthChannelInterceptor
        // rejects any SEND frame that has no authenticated user, and
        // principal.getName() is the user's UUID string, exactly like
        // Authentication.getName() in the REST controller.
        UUID driverUserId = UUID.fromString(principal.getName());

        tripLocationService.updateLocation(tripId, driverUserId, request);

        // No explicit reply needed: TripLocationServiceImpl already
        // broadcasts the update to /topic/trips/{tripId}/location for
        // every subscribed passenger.
    }

    // =========================================================
    // ERROR HANDLING
    // =========================================================
    // STOMP has no concept of an HTTP status code, so a failed SEND
    // never gets a synchronous error back to the caller by default -
    // it would otherwise fail silently. These handlers route any
    // exception raised above back to the *sending* driver's own
    // private queue, so the app can surface it (e.g. "trip not
    // active, GPS not accepted").
    // =========================================================

    @MessageExceptionHandler(ResourceNotFoundException.class)
    @SendToUser("/queue/errors")
    public WsErrorMessage handleNotFound(ResourceNotFoundException exception) {
        log.warn("WS location update rejected (not found): {}", exception.getMessage());
        return WsErrorMessage.of("NOT_FOUND", exception.getMessage(), "/app/trips/*/location");
    }

    @MessageExceptionHandler(BadRequestException.class)
    @SendToUser("/queue/errors")
    public WsErrorMessage handleBadRequest(BadRequestException exception) {
        log.warn("WS location update rejected (bad request): {}", exception.getMessage());
        return WsErrorMessage.of("BAD_REQUEST", exception.getMessage(), "/app/trips/*/location");
    }

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser("/queue/errors")
    public WsErrorMessage handleValidation(MethodArgumentNotValidException exception) {

        String detail = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse("Invalid GPS payload");

        log.warn("WS location update rejected (validation): {}", detail);
        return WsErrorMessage.of("VALIDATION_ERROR", detail, "/app/trips/*/location");
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public WsErrorMessage handleGeneric(Exception exception) {
        log.error("Unexpected WS location update failure", exception);
        return WsErrorMessage.of("INTERNAL_ERROR", "Could not process GPS update", "/app/trips/*/location");
    }
}
