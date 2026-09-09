package com.routesync.backend.controller;

import com.routesync.backend.dto.driver.DriverResponse;
import com.routesync.backend.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Driver related REST APIs.
 *
 * This controller handles driver information and operational
 * read APIs.
 *
 * Admin management APIs are handled separately by
 * AdminDriverController.
 */
@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    /**
     * Get driver by driver ID.
     *
     * ADMIN and DRIVER can access this endpoint.
     *
     * GET /api/v1/drivers/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<DriverResponse> getDriverById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                driverService.getDriverById(id)
        );
    }

    /**
     * Get driver profile by user ID.
     *
     * ADMIN and DRIVER can access this endpoint.
     *
     * GET /api/v1/drivers/user/{userId}
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<DriverResponse> getDriverByUserId(
            @PathVariable UUID userId
    ) {

        return ResponseEntity.ok(
                driverService.getDriverByUserId(userId)
        );
    }

    /**
     * Get all active drivers.
     *
     * Only ADMIN can access the complete driver list.
     *
     * GET /api/v1/drivers
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DriverResponse>> getAllActiveDrivers() {

        return ResponseEntity.ok(
                driverService.getAllActiveDrivers()
        );
    }
}