package com.routesync.backend.dto.trip.location;

import java.time.LocalDateTime;
import java.util.UUID;

public record TripLocationResponse(

        UUID id,

        UUID tripId,

        Double latitude,

        Double longitude,

        Double speed,

        Double heading,

        Double accuracy,

        LocalDateTime recordedAt
) {
}