package com.routesync.backend.controller.admin;

import com.routesync.backend.dto.route.CreateRouteRequest;
import com.routesync.backend.dto.route.RouteResponse;
import com.routesync.backend.dto.route.UpdateRouteRequest;
import com.routesync.backend.service.RouteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/routes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRouteController {

    private final RouteService routeService;

    /**
     * Create a new route.
     */
    @PostMapping
    public ResponseEntity<RouteResponse> createRoute(
            @Valid @RequestBody CreateRouteRequest request
    ) {

        RouteResponse response = routeService.createRoute(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get all routes.
     */
    @GetMapping
    public ResponseEntity<List<RouteResponse>> getAllRoutes() {

        // Currently RouteService exposes active routes.
        return ResponseEntity.ok(
                routeService.getAllActiveRoutes()
        );
    }

    /**
     * Update route.
     */
    @PutMapping("/{routeId}")
    public ResponseEntity<RouteResponse> updateRoute(
            @PathVariable UUID routeId,
            @Valid @RequestBody UpdateRouteRequest request
    ) {

        RouteResponse response =
                routeService.updateRoute(routeId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate route.: PATCH: /api/v1/admin/routes/{id}/deactivate
     */
    @PatchMapping("/{routeId}/deactivate")
    public ResponseEntity<Void> deactivateRoute(
            @PathVariable UUID routeId
    ) {

        routeService.deactivateRoute(routeId);

        return ResponseEntity.noContent().build();
    }
}