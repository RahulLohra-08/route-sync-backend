package com.routesync.backend.dto.trip;

import com.routesync.backend.entity.enums.TripStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TripResponse(

        UUID id,

        UUID routeId,

        String routeCode,

        String routeName,

        UUID busId,

        String busNumber,

        String registrationNumber,

        UUID driverId,

        String employeeCode,

        String driverName,

        LocalDateTime scheduledStartTime,

        LocalDateTime scheduledEndTime,

        LocalDateTime actualStartTime,

        LocalDateTime actualEndTime,

        TripStatus status,

        Integer totalSeats,

        Integer currentOccupancy,

        Integer availableSeats,

        Double currentLatitude,

        Double currentLongitude,

        LocalDateTime lastLocationUpdate,

        Boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}