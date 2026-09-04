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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    /**
     * Create a new driver.
     *
     * Only ADMIN can create drivers.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DriverResponse> createDriver(
            @Valid @RequestBody CreateDriverRequest request
    ) {

        DriverResponse response = driverService.createDriver(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get driver by driver ID.
     *
     * ADMIN and DRIVER can access this endpoint.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<DriverResponse> getDriverById(
            @PathVariable UUID id
    ) {

        DriverResponse response =
                driverService.getDriverById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Get driver by associated user ID.
     *
     * ADMIN and DRIVER can access this endpoint.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<DriverResponse> getDriverByUserId(
            @PathVariable UUID userId
    ) {

        DriverResponse response =
                driverService.getDriverByUserId(userId);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all active drivers.
     *
     * Only ADMIN can access the complete driver list.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DriverResponse>> getAllActiveDrivers() {

        List<DriverResponse> drivers = driverService.getAllActiveDrivers();

        return ResponseEntity.ok(drivers);
    }

    /**
     * Update driver.
     *
     * Only ADMIN can update driver information.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDriverRequest request
    ) {

        DriverResponse response = driverService.updateDriver(id, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate driver.
     *
     * Only ADMIN can deactivate a driver.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateDriver(
            @PathVariable UUID id
    ) {

        driverService.deactivateDriver(id);

        return ResponseEntity.noContent().build();
    }
}