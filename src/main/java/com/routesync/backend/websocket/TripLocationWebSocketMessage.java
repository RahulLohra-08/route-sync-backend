package com.routesync.backend.websocket;

import java.time.LocalDateTime;
import java.util.UUID;

public record TripLocationWebSocketMessage(

        UUID tripId,

        UUID busId,

        String busNumber,

        UUID routeId,

        String routeCode,

        Double latitude,

        Double longitude,

        Double speed,

        Double heading,

        Double accuracy,

        LocalDateTime recordedAt
) {
}