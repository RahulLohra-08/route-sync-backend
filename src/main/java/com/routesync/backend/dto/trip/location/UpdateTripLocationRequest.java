package com.routesync.backend.dto.trip.location;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateTripLocationRequest(

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        @Digits(integer = 2, fraction = 6)
        Double latitude,


        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
        @Digits(integer = 3, fraction = 6)
        Double longitude,


        @PositiveOrZero(message = "Speed cannot be negative")
        Double speed,


        @DecimalMin(value = "0.0", message = "Heading cannot be negative")
        @DecimalMax(value = "359.999999", message = "Heading must be less than 360")
        Double heading,


        @PositiveOrZero(message = "Accuracy cannot be negative")
        Double accuracy
) {
}