package com.routesync.backend.service.impl;

import com.routesync.backend.dto.route.CreateRouteRequest;
import com.routesync.backend.dto.route.RouteResponse;
import com.routesync.backend.dto.route.UpdateRouteRequest;
import com.routesync.backend.entity.Route;
import com.routesync.backend.entity.enums.RouteStatus;
import com.routesync.backend.exception.DuplicateResourceException;
import com.routesync.backend.exception.ResourceNotFoundException;
import com.routesync.backend.repository.RouteRepository;
import com.routesync.backend.service.RouteService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;

    /**
     * Create a new route.
     */
    @Override
    public RouteResponse createRoute(CreateRouteRequest request) {

        String routeCode = normalizeRouteCode(request.routeCode());

        // Check duplicate route code.
        if (routeRepository.existsByRouteCode(routeCode)) {
            throw new DuplicateResourceException(
                    "Route already exists with code: " + routeCode
            );
        }

        Route route = new Route();

        route.setRouteCode(routeCode);
        route.setRouteName(request.routeName().trim());
        route.setStartLocation(request.startLocation().trim());
        route.setEndLocation(request.endLocation().trim());
        route.setDistanceKm(request.distanceKm());
        route.setEstimatedDurationMinutes(
                request.estimatedDurationMinutes()
        );

        route.setStatus(RouteStatus.ACTIVE);
        route.setActive(true);

        Route savedRoute = routeRepository.save(route);

        return mapToResponse(savedRoute);
    }

    @Override
    public List<RouteResponse> getAllRoutes() {
        return routeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get route by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public RouteResponse getRouteById(UUID routeId) {

        Route route = findRouteById(routeId);

        return mapToResponse(route);
    }

    /**
     * Get all active routes.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> getAllActiveRoutes() {

        return routeRepository.findAllByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Search routes by route name.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> searchRoutes(String routeName) {

        if (routeName == null || routeName.isBlank()) {
            return getAllActiveRoutes();
        }

        return routeRepository
                .findByRouteNameContainingIgnoreCase(routeName.trim())
                .stream()
                .filter(route -> Boolean.TRUE.equals(route.getActive()))
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Update route details.
     */
    @Override
    public RouteResponse updateRoute(
            UUID routeId,
            UpdateRouteRequest request
    ) {

        Route route = findRouteById(routeId);

        // Update route code only when provided.
        if (request.routeCode() != null
                && !request.routeCode().isBlank()) {

            String newRouteCode =
                    normalizeRouteCode(request.routeCode());

            if (!newRouteCode.equals(route.getRouteCode())
                    && routeRepository.existsByRouteCodeAndIdNot(
                    newRouteCode,
                    routeId
            )) {

                throw new DuplicateResourceException(
                        "Route already exists with code: "
                                + newRouteCode
                );
            }

            route.setRouteCode(newRouteCode);
        }

        if (request.routeName() != null
                && !request.routeName().isBlank()) {

            route.setRouteName(request.routeName().trim());
        }

        if (request.startLocation() != null
                && !request.startLocation().isBlank()) {

            route.setStartLocation(request.startLocation().trim());
        }

        if (request.endLocation() != null
                && !request.endLocation().isBlank()) {

            route.setEndLocation(request.endLocation().trim());
        }

        if (request.distanceKm() != null) {
            route.setDistanceKm(request.distanceKm());
        }

        if (request.estimatedDurationMinutes() != null) {
            route.setEstimatedDurationMinutes(
                    request.estimatedDurationMinutes()
            );
        }

        if (request.status() != null) {
            route.setStatus(request.status());
        }

        if (request.active() != null) {
            route.setActive(request.active());
        }

        Route updatedRoute = routeRepository.save(route);

        return mapToResponse(updatedRoute);
    }

    /**
     * Deactivate route.
     *
     * Soft delete is used instead of deleting database record.
     */
    @Override
    public void deactivateRoute(UUID routeId) {

        log.info("Ruoute ID"+ routeId);
        Route route = findRouteById(routeId);

        route.setActive(false);
        route.setStatus(RouteStatus.INACTIVE);

        routeRepository.save(route);
    }

    /**
     * Find route or throw exception.
     */
    private Route findRouteById(UUID routeId) {

        return routeRepository.findById(routeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Route not found with ID: " + routeId
                        )
                );
    }

    /**
     * Normalize route code.
     *
     * Example:
     * " rt-001 " → "RT-001"
     */
    private String normalizeRouteCode(String routeCode) {

        return routeCode.trim().toUpperCase();
    }

    /**
     * Convert Route entity to RouteResponse DTO.
     */
    private RouteResponse mapToResponse(Route route) {

        return new RouteResponse(
                route.getId(),
                route.getRouteCode(),
                route.getRouteName(),
                route.getStartLocation(),
                route.getEndLocation(),
                route.getDistanceKm(),
                route.getEstimatedDurationMinutes(),
                route.getStatus(),
                route.getActive(),
                route.getCreatedAt(),
                route.getUpdatedAt()
        );
    }
}