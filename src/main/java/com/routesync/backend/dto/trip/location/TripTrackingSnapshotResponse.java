
package com.routesync.backend.dto.trip.location;

import com.routesync.backend.entity.enums.TripStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Passenger tracking snapshot.
 *
 * Henglish:
 * Is response se passenger ko trip ki current details aur
 * latest known GPS location ek hi API call mein mil jayegi.
 */
public record TripTrackingSnapshotResponse(
        UUID tripId,
        TripStatus status,
        UUID busId,
        String busNumber,
        UUID routeId,
        String routeCode,
        Double latitude,
        Double longitude,
        Double speed,
        Double heading,
        Double accuracy,
        LocalDateTime lastLocationUpdate,
        boolean locationAvailable,
        boolean locationFresh
) {
}
