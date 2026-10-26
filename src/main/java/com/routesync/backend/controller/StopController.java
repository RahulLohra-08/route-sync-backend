package com.routesync.backend.controller;

import com.routesync.backend.dto.stop.StopResponse;
import com.routesync.backend.service.StopService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Read-only Stop APIs.
 *
 * These APIs are available to authenticated users,
 * including PASSENGER and DRIVER.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StopController {

    private final StopService stopService;

    /**
     * Get all stops belonging to a route.
     *
     * GET /api/v1/routes/{routeId}/stops
     *
     * Stops are returned in ascending stopOrder.
     */
    @GetMapping("/routes/{routeId}/stops")
    public ResponseEntity<List<StopResponse>> getStopsByRoute(
            @PathVariable UUID routeId
    ) {

        return ResponseEntity.ok(
                stopService.getStopsByRoute(routeId)
        );
    }

    /**
     * Get only active stops belonging to a route.
     *
     * GET /api/v1/routes/{routeId}/stops/active
     */
    @GetMapping("/routes/{routeId}/stops/active")
    public ResponseEntity<List<StopResponse>> getActiveStopsByRoute(
            @PathVariable UUID routeId
    ) {

        return ResponseEntity.ok(
                stopService.getActiveStopsByRoute(routeId)
        );
    }

    /**
     * Get a single stop by ID.
     *
     * GET /api/v1/stops/{stopId}
     */
    @GetMapping("/stops/{stopId}")
    public ResponseEntity<StopResponse> getStopById(
            @PathVariable UUID stopId
    ) {

        return ResponseEntity.ok(
                stopService.getStopById(stopId)
        );
    }
}