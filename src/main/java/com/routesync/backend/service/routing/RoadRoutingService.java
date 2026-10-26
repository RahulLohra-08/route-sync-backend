package com.routesync.backend.service.routing;

import java.util.List;
import java.util.Optional;

public interface RoadRoutingService {

    /**
     * Returns road-network distance in kilometres.
     *
     * Empty means the routing provider could not calculate
     * a valid route. Callers must not silently treat that
     * failure as a successful road-distance calculation.
     */
    Optional<Double> calculateRoadDistanceKm(
            List<RouteCoordinate> coordinates
    );
}