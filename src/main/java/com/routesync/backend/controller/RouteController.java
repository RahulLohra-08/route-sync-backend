package com.routesync.backend.controller;

import com.routesync.backend.dto.route.RouteResponse;
import com.routesync.backend.service.RouteService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    /**
     * Get route by ID.
     */
    @GetMapping("/{routeId}")
    public ResponseEntity<RouteResponse> getRouteById(
            @PathVariable UUID routeId
    ) {

        return ResponseEntity.ok(
                routeService.getRouteById(routeId)
        );
    }

    /**
     * Get all active routes.
     */
    @GetMapping("/active")
    public ResponseEntity<List<RouteResponse>> getActiveRoutes() {

        return ResponseEntity.ok(
                routeService.getAllActiveRoutes()
        );
    }

    /**
     * Search active routes by route name.
     */
    @GetMapping("/search")
    public ResponseEntity<List<RouteResponse>> searchRoutes(
            @RequestParam(required = false) String routeName
    ) {

        return ResponseEntity.ok(
                routeService.searchRoutes(routeName)
        );
    }
}