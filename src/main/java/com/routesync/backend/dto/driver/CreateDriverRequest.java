package com.routesync.backend.dto.driver;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateDriverRequest(

        @NotNull(message = "User ID is required")
        UUID userId,

        @NotBlank(message = "Employee code is required")
        @Size(max = 30, message = "Employee code cannot exceed 30 characters")
        String employeeCode,

        @NotBlank(message = "License number is required")
        @Size(max = 50, message = "License number cannot exceed 50 characters")
        String licenseNumber,

        @NotNull(message = "License expiry date is required")
        @FutureOrPresent(message = "License expiry date must be today or a future date")
        LocalDate licenseExpiryDate
) {
}