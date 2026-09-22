package com.routesync.backend.dto.stop;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateStopRequest(

        @NotBlank(message = "Stop name is required")
        @Size(max = 150, message = "Stop name cannot exceed 150 characters")
        String stopName,

        @Size(max = 255, message = "Address cannot exceed 255 characters")
        String address,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        @Digits(integer = 2, fraction = 6,
                message = "Latitude can have maximum 6 decimal places")
        Double latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
        @Digits(integer = 3, fraction = 6,
                message = "Longitude can have maximum 6 decimal places")
        Double longitude,

        @NotNull(message = "Stop order is required")
        @Positive(message = "Stop order must be greater than 0")
        Integer stopOrder,

        @Positive(message = "Arrival offset must be greater than 0")
        Integer estimatedArrivalOffsetMinutes
) {
}