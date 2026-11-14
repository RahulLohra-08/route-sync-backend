package com.routesync.backend.dto.trip;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Body of PATCH /api/v1/driver/trips/{tripId}/occupancy.
 * The upper bound (total seats of the trip) is checked in the service.
 */
public record UpdateOccupancyRequest(
        @NotNull(message = "Current occupancy is required")
        @Min(value = 0, message = "Current occupancy cannot be negative")
        Integer currentOccupancy
) {
}
