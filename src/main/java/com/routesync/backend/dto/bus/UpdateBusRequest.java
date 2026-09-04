package com.routesync.backend.dto.bus;

import com.routesync.backend.entity.enums.BusStatus;
import com.routesync.backend.entity.enums.BusType;
import com.routesync.backend.entity.enums.FuelType;
import jakarta.validation.constraints.*;

public record UpdateBusRequest(

        @Size(max = 20, message = "Registration number cannot exceed 20 characters")
        String registrationNumber,

        @Size(max = 20, message = "Bus number cannot exceed 20 characters")
        String busNumber,

        @Size(max = 100, message = "Model cannot exceed 100 characters")
        String model,

        @Size(max = 100, message = "Manufacturer cannot exceed 100 characters")
        String manufacturer,

        @Min(value = 1, message = "Capacity must be at least 1")
        @Max(value = 500, message = "Capacity cannot exceed 500")
        Integer capacity,

        BusType busType,

        FuelType fuelType,

        BusStatus status,

        @Min(value = 1950, message = "Invalid manufacturing year")
        @Max(value = 2100, message = "Invalid manufacturing year")
        Integer yearOfManufacture
) {
}