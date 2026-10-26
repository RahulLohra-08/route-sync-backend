
package com.routesync.backend.dto.trip.location;

import java.time.LocalDateTime;
import java.util.UUID;

public record TripEtaResponse(
        UUID tripId,
        UUID stopId,
        String stopName,
        Double distanceKm,
        Double etaMinutes,
        LocalDateTime estimatedArrivalAt,
        boolean locationAvailable,
        boolean locationFresh,
        String calculationMethod,
        String message
) {
}
