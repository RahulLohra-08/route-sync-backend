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
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.security.Principal;
import java.util.UUID;

/**
 * Driver GPS WebSocket endpoint.
 *
 * Client SEND:
 *
 * /app/trips/{tripId}/location
 *
 * Backend:
 *
 * JWT → authorization → service → DB → AFTER_COMMIT broadcast
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class DriverLocationSocketController {

    private final TripLocationService tripLocationService;

    @MessageMapping("/trips/{tripId}/location")
    @PreAuthorize("hasRole('DRIVER')")
    public void updateLocation(
            @DestinationVariable UUID tripId,
            @Valid @Payload UpdateTripLocationRequest request,
            Principal principal
    ) {

        UUID driverUserId = UUID.fromString(principal.getName());

        /*
         * Henglish:
         *
         * Controller business logic nahi rakhega.
         * Same TripLocationService use hoga jo REST endpoint use karta hai.
         *
         * Isse REST aur WebSocket dono mein same:
         * - validation
         * - driver verification
         * - trip verification
         * - DB persistence
         * - broadcast
         * rules rahenge.
         */
        tripLocationService.updateLocation(
                tripId,
                driverUserId,
                request
        );
    }

    // =========================================================
    // NOT FOUND
    // =========================================================

    @MessageExceptionHandler(ResourceNotFoundException.class)
    @SendToUser("/queue/errors")
    public WsErrorMessage handleNotFound(
            ResourceNotFoundException exception
    ) {

        log.warn(
                "WS location NOT_FOUND: {}",
                exception.getMessage()
        );

        return WsErrorMessage.of(
                "NOT_FOUND",
                exception.getMessage(),
                "/app/trips/{tripId}/location"
        );
    }

    // =========================================================
    // BAD REQUEST
    // =========================================================

    @MessageExceptionHandler(BadRequestException.class)
    @SendToUser("/queue/errors")
    public WsErrorMessage handleBadRequest(
            BadRequestException exception
    ) {

        log.warn(
                "WS location BAD_REQUEST: {}",
                exception.getMessage()
        );

        return WsErrorMessage.of(
                "BAD_REQUEST",
                exception.getMessage(),
                "/app/trips/{tripId}/location"
        );
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser("/queue/errors")
    public WsErrorMessage handleValidation(
            MethodArgumentNotValidException exception
    ) {

        String detail =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .findFirst()
                        .map(error ->
                                error.getField()
                                        + ": "
                                        + error.getDefaultMessage()
                        )
                        .orElse(
                                "Invalid GPS payload"
                        );

        return WsErrorMessage.of(
                "VALIDATION_ERROR",
                detail,
                "/app/trips/{tripId}/location"
        );
    }

    // =========================================================
    // UNKNOWN ERROR
    // =========================================================

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public WsErrorMessage handleGeneric(
            Exception exception
    ) {

        log.error(
                "Unexpected WebSocket error",
                exception
        );

        return WsErrorMessage.of(
                "INTERNAL_ERROR",
                "Could not process GPS update",
                "/app/trips/{tripId}/location"
        );
    }
}