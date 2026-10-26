package com.routesync.backend.dto.route;

import com.routesync.backend.entity.enums.RouteStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record RouteResponse(

        UUID id,

        String routeCode,

        String routeName,

        String startLocation,

        String endLocation,

        Double distanceKm,

        Integer estimatedDurationMinutes,

        RouteStatus status,

        Boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}