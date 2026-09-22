package com.routesync.backend.controller.admin;

import com.routesync.backend.dto.driver.CreateDriverRequest;
import com.routesync.backend.dto.driver.DriverResponse;
import com.routesync.backend.dto.driver.UpdateDriverRequest;
import com.routesync.backend.dto.stop.CreateStopRequest;
import com.routesync.backend.dto.stop.StopResponse;
import com.routesync.backend.dto.stop.UpdateStopRequest;
import com.routesync.backend.service.DriverService;
import com.routesync.backend.service.StopService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin APIs for Stop Management.
 *
 * Only ADMIN users are allowed to:
 * - Create stops
 * - Update stops
 * - Deactivate stops
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStopController {

    private final StopService stopService;

    /**
     * Create a new stop under a route.
     *
     * POST /api/v1/admin/routes/{routeId}/stops
     */
    @PostMapping("/routes/{routeId}/stops")
    public ResponseEntity<StopResponse> createStop(
            @PathVariable UUID routeId,
            @Valid @RequestBody CreateStopRequest request
    ) {

        StopResponse response = stopService.createStop(routeId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Update an existing stop.
     *
     * PUT /api/v1/admin/stops/{stopId}
     */
    @PutMapping("/stops/{stopId}")
    public ResponseEntity<StopResponse> updateStop(
            @PathVariable UUID stopId,
            @Valid @RequestBody UpdateStopRequest request
    ) {

        StopResponse response =
                stopService.updateStop(stopId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Soft deactivate a stop.
     *
     * PATCH /api/v1/admin/stops/{stopId}/deactivate
     *
     * The stop remains in the database.
     * active = false
     * status = INACTIVE
     */
    @PatchMapping("/stops/{stopId}/deactivate")
    public ResponseEntity<Void> deactivateStop(
            @PathVariable UUID stopId
    ) {

        stopService.deactivateStop(stopId);

        return ResponseEntity.noContent().build();
    }

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
    public static class AdminDriverController {

        private final DriverService driverService;

        @GetMapping("/test")
        public ResponseEntity<String> testEndpoint() {

            System.out.println("=======================x=============");
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
}