package com.routesync.backend.service.routing;

/**
 * Latitude/longitude of a point on the route.
 */
public record RouteCoordinate(
        double latitude,
        double longitude
) {
}