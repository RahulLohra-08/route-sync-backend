package com.routesync.backend.dto.trip;

import com.routesync.backend.entity.enums.TripStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateTripRequest(

        UUID routeId,

        UUID busId,

        UUID driverId,

        @Future(message = "Scheduled start time must be in the future")
        LocalDateTime scheduledStartTime,

        @Future(message = "Scheduled end time must be in the future")
        LocalDateTime scheduledEndTime,

        TripStatus status,

        @Min(value = 0, message = "Current occupancy cannot be negative")
        Integer currentOccupancy
) {
}