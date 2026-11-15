package com.routesync.backend.service;

import com.routesync.backend.dto.route.CreateRouteRequest;
import com.routesync.backend.dto.route.RouteResponse;
import com.routesync.backend.dto.route.UpdateRouteRequest;

import java.util.List;
import java.util.UUID;

public interface RouteService {

    RouteResponse createRoute(CreateRouteRequest request);

    RouteResponse getRouteById(UUID routeId);

    List<RouteResponse> getAllActiveRoutes();


    List<RouteResponse> getAllRoutes();

    List<RouteResponse> searchRoutes(String routeName);

    RouteResponse updateRoute(
            UUID routeId,
            UpdateRouteRequest request
    );

    void deactivateRoute(UUID routeId);
}