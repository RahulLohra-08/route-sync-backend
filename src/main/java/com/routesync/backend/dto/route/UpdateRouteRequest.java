package com.routesync.backend.dto.route;

import com.routesync.backend.entity.enums.RouteStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateRouteRequest(

        @Size(max = 30, message = "Route code cannot exceed 30 characters")
        String routeCode,

        @Size(max = 150, message = "Route name cannot exceed 150 characters")
        String routeName,

        @Size(max = 150, message = "Start location cannot exceed 150 characters")
        String startLocation,

        @Size(max = 150, message = "End location cannot exceed 150 characters")
        String endLocation,

        @DecimalMin(value = "0.1", message = "Distance must be greater than 0")
        @Digits(integer = 8, fraction = 2,
                message = "Distance can have maximum 8 integer digits and 2 decimal digits")
        Double distanceKm,

        @Positive(message = "Estimated duration must be greater than 0")
        Integer estimatedDurationMinutes,

        RouteStatus status,

        Boolean active
) {
}