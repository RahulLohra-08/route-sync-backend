package com.routesync.backend.dto.trip;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTripRequest(

        @NotNull(message = "Route ID is required")
        UUID routeId,

        @NotNull(message = "Bus ID is required")
        UUID busId,

        @NotNull(message = "Driver ID is required")
        UUID driverId,

        @NotNull(message = "Scheduled start time is required")
        @Future(message = "Scheduled start time must be in the future")
        LocalDateTime scheduledStartTime,

        @NotNull(message = "Scheduled end time is required")
        @Future(message = "Scheduled end time must be in the future")
        LocalDateTime scheduledEndTime
) {
}