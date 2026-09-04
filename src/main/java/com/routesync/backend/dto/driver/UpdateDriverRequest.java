package com.routesync.backend.dto.driver;

import com.routesync.backend.entity.enums.DriverStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateDriverRequest(

        @Size(max = 30, message = "Employee code cannot exceed 30 characters")
        String employeeCode,

        @Size(max = 50, message = "License number cannot exceed 50 characters")
        String licenseNumber,

        @FutureOrPresent(message = "License expiry date must be today or a future date")
        LocalDate licenseExpiryDate,

        DriverStatus status
) {
}