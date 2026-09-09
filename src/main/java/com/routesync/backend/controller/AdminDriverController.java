package com.routesync.backend.controller;

import com.routesync.backend.dto.driver.CreateDriverRequest;
import com.routesync.backend.dto.driver.DriverResponse;
import com.routesync.backend.dto.driver.UpdateDriverRequest;
import com.routesync.backend.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin-only REST APIs for Driver Management.
 *
 * Responsibilities:
 * - Create driver profiles
 * - Update driver profiles
 * - Deactivate driver profiles
 *
 * All endpoints in this controller are restricted to ADMIN users.
 */
@RestController
@RequestMapping("/api/v1/admin/drivers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDriverController {

    private final DriverService driverService;

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {

        System.out.println("====================================");
        System.out.println("ADMIN DRIVER CONTROLLER HIT");
        System.out.println("====================================");

        return ResponseEntity.ok("AdminDriverController is working");
    }

    /**
     * Create a new driver profile.
     *
     * POST /api/v1/admin/drivers
     */
    @PostMapping
    public ResponseEntity<DriverResponse> createDriver(
             @RequestBody CreateDriverRequest request
    ) {

        System.out.println("========================================");
        System.out.println("CREATE DRIVER CONTROLLER HIT");
        System.out.println("Request: " + request);
        System.out.println("========================================");

        DriverResponse response = driverService.createDriver(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Update an existing driver profile.
     *
     * PUT /api/v1/admin/drivers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDriverRequest request
    ) {

        DriverResponse response = driverService.updateDriver(id, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a driver.
     *
     * This performs a soft delete.
     *
     * DELETE /api/v1/admin/drivers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateDriver(
            @PathVariable UUID id
    ) {

        driverService.deactivateDriver(id);

        return ResponseEntity.noContent().build();
    }
}