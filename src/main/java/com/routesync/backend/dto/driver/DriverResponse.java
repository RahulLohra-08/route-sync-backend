package com.routesync.backend.dto.driver;

import com.routesync.backend.entity.enums.DriverStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DriverResponse(

        UUID id,

        UUID userId,

        String employeeCode,

        String licenseNumber,

        LocalDate licenseExpiryDate,

        DriverStatus status,

        Boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}