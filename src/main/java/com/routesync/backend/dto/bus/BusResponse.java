package com.routesync.backend.dto.bus;

import com.routesync.backend.entity.enums.BusStatus;
import com.routesync.backend.entity.enums.BusType;
import com.routesync.backend.entity.enums.FuelType;

import java.time.LocalDateTime;
import java.util.UUID;

public record BusResponse(

        UUID id,

        String registrationNumber,

        String busNumber,

        String model,

        String manufacturer,

        Integer capacity,

        BusType busType,

        FuelType fuelType,

        BusStatus status,

        Integer yearOfManufacture,

        Double currentLatitude,

        Double currentLongitude,

        LocalDateTime lastLocationUpdate,

        Boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}