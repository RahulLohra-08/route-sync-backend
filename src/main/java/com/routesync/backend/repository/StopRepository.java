package com.routesync.backend.repository;

import com.routesync.backend.entity.Stop;
import com.routesync.backend.entity.enums.StopStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StopRepository extends JpaRepository<Stop, UUID> {

    /**
     * Find all stops belonging to a specific route.
     */
    List<Stop> findAllByRouteIdOrderByStopOrderAsc(UUID routeId);

    /**
     * Find all active stops of a route
     * in their correct sequence.
     */
    List<Stop> findAllByRouteIdAndActiveTrueOrderByStopOrderAsc(
            UUID routeId
    );

    /**
     * Find a specific stop by route and stop order.
     */
    Optional<Stop> findByRouteIdAndStopOrder(
            UUID routeId,
            Integer stopOrder
    );

    /**
     * Check whether a stop order already exists
     * inside a particular route.
     */
    boolean existsByRouteIdAndStopOrder(
            UUID routeId,
            Integer stopOrder
    );

    /**
     * Check duplicate stop order during update.
     *
     * Current stop ID is excluded.
     */
    boolean existsByRouteIdAndStopOrderAndIdNot(
            UUID routeId,
            Integer stopOrder,
            UUID stopId
    );

    /**
     * Find stops by route and status.
     */
    List<Stop> findAllByRouteIdAndStatusOrderByStopOrderAsc(
            UUID routeId,
            StopStatus status
    );

    /**
     * Count stops belonging to a route.
     */
    long countByRouteId(UUID routeId);

    /**
     * Count active stops belonging to a route.
     */
    long countByRouteIdAndActiveTrue(UUID routeId);
}