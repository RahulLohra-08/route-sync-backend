package com.routesync.backend.repository;

import com.routesync.backend.entity.Route;
import com.routesync.backend.entity.enums.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RouteRepository extends JpaRepository<Route, UUID> {

    /**
     * Find route using unique route code.
     */
    Optional<Route> findByRouteCode(String routeCode);

    /**
     * Check whether route code already exists.
     */
    boolean existsByRouteCode(String routeCode);

    /**
     * Check duplicate route code during update.
     *
     * Excludes the current route ID.
     */
    boolean existsByRouteCodeAndIdNot(
            String routeCode,
            UUID id
    );

    /**
     * Fetch all active routes.
     */
    List<Route> findAllByActiveTrue();

    /**
     * Fetch routes by status.
     */
    List<Route> findAllByStatus(RouteStatus status);

    /**
     * Fetch active routes by status.
     */
    List<Route> findAllByActiveTrueAndStatus(
            RouteStatus status
    );

    /**
     * Search routes by route name.
     */
    List<Route> findByRouteNameContainingIgnoreCase(
            String routeName
    );

    /**
     * Search routes by start and end location.
     */
    List<Route> findByStartLocationContainingIgnoreCaseAndEndLocationContainingIgnoreCase(
            String startLocation,
            String endLocation
    );
}