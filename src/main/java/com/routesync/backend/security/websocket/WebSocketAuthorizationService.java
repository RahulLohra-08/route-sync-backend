package com.routesync.backend.security.websocket;

import com.routesync.backend.entity.Trip;
import com.routesync.backend.entity.UserRole;
import com.routesync.backend.exception.BadRequestException;
import com.routesync.backend.exception.ResourceNotFoundException;
import com.routesync.backend.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WebSocket destination-level authorization.
 *
 * Henglish:
 *
 * JWT valid hona enough nahi hai.
 *
 * Example:
 *
 * Passenger A ka JWT valid hai.
 * Lekin iska matlab ye nahi hai ki woh arbitrary
 * internal/admin WebSocket destination access kar sake.
 *
 * Is service mein hum check karenge:
 *
 * 1. Destination valid hai?
 * 2. Trip exist karta hai?
 * 3. Trip active hai?
 * 4. User ka role kya hai?
 * 5. Driver hai to kya ye trip usi driver ko assigned hai?
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebSocketAuthorizationService {

    private final TripRepository tripRepository;

    private static final Pattern TRIP_LOCATION_TOPIC =
            Pattern.compile(
                    "^/topic/trips/([0-9a-fA-F-]{36})/location$"
            );

    private static final Pattern TRIP_LOCATION_SEND =
            Pattern.compile(
                    "^/app/trips/([0-9a-fA-F-]{36})/location$"
            );

    public void authorizeSubscribe(
            Authentication authentication,
            String destination
    ) {

        /*
         * Private user queues ko destination-level trip check
         * ki zaroorat nahi hai.
         */
        if (destination.equals("/user/queue/errors")
                || destination.equals("/user/queue/location-ack")) {

            return;
        }

        Matcher matcher = TRIP_LOCATION_TOPIC.matcher(destination);

        if (!matcher.matches()) {

            throw new BadRequestException(
                    "Subscription destination is not allowed"
            );
        }

        UUID tripId = UUID.fromString(matcher.group(1));

        Trip trip =
                tripRepository.findByIdAndActiveTrue(tripId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Active trip not found: "
                                                + tripId
                                )
                        );

        authorizeTripViewer(
                authentication,
                trip
        );
    }

    public void authorizeSend(
            Authentication authentication,
            String destination
    ) {

        Matcher matcher =
                TRIP_LOCATION_SEND.matcher(destination);

        if (!matcher.matches()) {

            throw new BadRequestException(
                    "SEND destination is not allowed"
            );
        }

        /*
         * GPS ingestion sirf DRIVER karega.
         */
        if (!hasRole(authentication, UserRole.DRIVER)) {

            throw new BadRequestException(
                    "Only DRIVER can send GPS location"
            );
        }

        UUID tripId =
                UUID.fromString(matcher.group(1));

        Trip trip =
                tripRepository.findByIdAndActiveTrue(tripId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Active trip not found: "
                                                + tripId
                                )
                        );

        UUID userId =
                UUID.fromString(authentication.getName());

        /*
         * Henglish:
         *
         * Driver ko sirf apne assigned trip ka GPS bhejne denge.
         */
        if (!trip.getDriver()
                .getUser()
                .getId()
                .equals(userId)) {

            throw new BadRequestException(
                    "Driver is not assigned to this trip"
            );
        }
    }

    private void authorizeTripViewer(
            Authentication authentication,
            Trip trip
    ) {

        /*
         * ADMIN ko active trip monitoring allowed hai.
         */
        if (hasRole(authentication, UserRole.ADMIN)) {
            return;
        }

        /*
         * Passenger live public transport tracking kar sakta hai.
         */
        if (hasRole(authentication, UserRole.PASSENGER)) {
            return;
        }

        /*
         * Driver apna assigned trip dekh sakta hai.
         */
        if (hasRole(authentication, UserRole.DRIVER)) {

            UUID userId = UUID.fromString(authentication.getName());

            if (trip.getDriver()
                    .getUser()
                    .getId()
                    .equals(userId)) {

                return;
            }
        }

        throw new BadRequestException(
                "You are not authorized to track this trip"
        );
    }

    private boolean hasRole(
            Authentication authentication,
            UserRole role
    ) {

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals(
                                        "ROLE_" + role.name()
                                )
                );
    }
}