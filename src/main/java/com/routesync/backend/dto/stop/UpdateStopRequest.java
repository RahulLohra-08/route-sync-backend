package com.routesync.backend.dto.stop;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateStopRequest(

        @Size(max = 150, message = "Stop name cannot exceed 150 characters")
        String stopName,

        @Size(max = 255, message = "Address cannot exceed 255 characters")
        String address,

        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        @Digits(integer = 2, fraction = 6,
                message = "Latitude can have maximum 6 decimal places")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude can be <= 180")
        @Digits(integer = 3, fraction = 6,
                message = "Longitude can have maximum 6 decimal places")
        Double longitude,

        @Positive(message = "Stop order must be greater than 0")
        Integer stopOrder,

        @Positive(message = "Arrival offset must be greater than 0")
        Integer estimatedArrivalOffsetMinutes
) {
}