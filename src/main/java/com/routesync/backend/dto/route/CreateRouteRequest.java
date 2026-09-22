package com.routesync.backend.dto.route;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateRouteRequest(

        @NotBlank(message = "Route code is required")
        @Size(max = 30, message = "Route code cannot exceed 30 characters")
        String routeCode,

        @NotBlank(message = "Route name is required")
        @Size(max = 150, message = "Route name cannot exceed 150 characters")
        String routeName,

        @NotBlank(message = "Start location is required")
        @Size(max = 150, message = "Start location cannot exceed 150 characters")
        String startLocation,

        @NotBlank(message = "End location is required")
        @Size(max = 150, message = "End location cannot exceed 150 characters")
        String endLocation,

        @NotNull(message = "Distance is required")
        @DecimalMin(value = "0.1", message = "Distance must be greater than 0")
        @Digits(integer = 8, fraction = 2,
                message = "Distance can have maximum 8 integer digits and 2 decimal digits")
        Double distanceKm,

        @NotNull(message = "Estimated duration is required")
        @Positive(message = "Estimated duration must be greater than 0")
        Integer estimatedDurationMinutes
) {
}