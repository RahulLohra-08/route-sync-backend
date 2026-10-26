package com.routesync.backend.service.impl;

import com.routesync.backend.dto.stop.CreateStopRequest;
import com.routesync.backend.dto.stop.StopResponse;
import com.routesync.backend.dto.stop.UpdateStopRequest;
import com.routesync.backend.entity.Route;
import com.routesync.backend.entity.Stop;
import com.routesync.backend.entity.enums.StopStatus;
import com.routesync.backend.exception.DuplicateResourceException;
import com.routesync.backend.exception.ResourceNotFoundException;
import com.routesync.backend.repository.RouteRepository;
import com.routesync.backend.repository.StopRepository;
import com.routesync.backend.service.StopService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StopServiceImpl implements StopService {

    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;

    /**
     * Create a new stop under a route.
     */
    @Override
    public StopResponse createStop(
            UUID routeId,
            CreateStopRequest request
    ) {

        /*
         * Step 1:
         * Make sure the parent route exists.
         */
        Route route = findRouteById(routeId);

        /*
         * Step 2:
         * Stop order must be unique inside a route.
         */
        if (stopRepository.existsByRouteIdAndStopOrder(
                routeId,
                request.stopOrder()
        )) {

            throw new DuplicateResourceException(
                    "Stop order " + request.stopOrder()
                            + " already exists for route: "
                            + route.getRouteCode()
            );
        }

        /*
         * Step 3:
         * Create stop entity.
         */
        Stop stop = new Stop();

        stop.setRoute(route);
        stop.setStopName(request.stopName().trim());

        if (request.address() != null) {
            stop.setAddress(request.address().trim());
        }

        stop.setLatitude(request.latitude());
        stop.setLongitude(request.longitude());
        stop.setStopOrder(request.stopOrder());
        stop.setEstimatedArrivalOffsetMinutes(
                request.estimatedArrivalOffsetMinutes()
        );

        stop.setStatus(StopStatus.ACTIVE);
        stop.setActive(true);

        /*
         * Step 4:
         * Save stop.
         */
        Stop savedStop = stopRepository.save(stop);

        return mapToResponse(savedStop);
    }

    /**
     * Get stop by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public StopResponse getStopById(UUID stopId) {

        Stop stop = findStopById(stopId);

        return mapToResponse(stop);
    }

    /**
     * Get all stops of a route.
     *
     * Repository already sorts them by stopOrder ASC.
     */
    @Override
    @Transactional(readOnly = true)
    public List<StopResponse> getStopsByRoute(UUID routeId) {

        /*
         * Make sure route exists before querying stops.
         */
        findRouteById(routeId);

        return stopRepository
                .findAllByRouteIdOrderByStopOrderAsc(routeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get only active stops of a route.
     */
    @Override
    @Transactional(readOnly = true)
    public List<StopResponse> getActiveStopsByRoute(UUID routeId) {

        findRouteById(routeId);

        return stopRepository
                .findAllByRouteIdAndActiveTrueOrderByStopOrderAsc(
                        routeId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Update stop details.
     *
     * Supports partial update.
     */
    @Override
    public StopResponse updateStop(
            UUID stopId,
            UpdateStopRequest request
    ) {

        Stop stop = findStopById(stopId);

        UUID routeId = stop.getRoute().getId();

        /*
         * Update stop order only when provided.
         */
        if (request.stopOrder() != null) {

            if (!request.stopOrder()
                    .equals(stop.getStopOrder())) {

                boolean duplicate =
                        stopRepository
                                .existsByRouteIdAndStopOrderAndIdNot(
                                        routeId,
                                        request.stopOrder(),
                                        stopId
                                );

                if (duplicate) {

                    throw new DuplicateResourceException(
                            "Stop order "
                                    + request.stopOrder()
                                    + " already exists for this route"
                    );
                }
            }

            stop.setStopOrder(request.stopOrder());
        }

        /*
         * Update stop name.
         */
        if (request.stopName() != null
                && !request.stopName().isBlank()) {

            stop.setStopName(
                    request.stopName().trim()
            );
        }

        /*
         * Update address.
         */
        if (request.address() != null) {

            stop.setAddress(
                    request.address().trim()
            );
        }

        /*
         * Update coordinates.
         */
        if (request.latitude() != null) {
            stop.setLatitude(request.latitude());
        }

        if (request.longitude() != null) {
            stop.setLongitude(request.longitude());
        }

        /*
         * Update estimated arrival offset.
         */
        if (request.estimatedArrivalOffsetMinutes() != null) {

            stop.setEstimatedArrivalOffsetMinutes(
                    request.estimatedArrivalOffsetMinutes()
            );
        }

        Stop updatedStop = stopRepository.save(stop);

        return mapToResponse(updatedStop);
    }

    /**
     * Deactivate stop.
     *
     * Soft delete is used.
     */
    @Override
    public void deactivateStop(UUID stopId) {

        Stop stop = findStopById(stopId);

        /*
         * Make operation idempotent.
         *
         * If already inactive, nothing needs to be changed.
         */
        if (Boolean.FALSE.equals(stop.getActive())) {
            return;
        }

        stop.setActive(false);
        stop.setStatus(StopStatus.INACTIVE);

        stopRepository.save(stop);
    }

    /**
     * Find route or throw 404.
     */
    private Route findRouteById(UUID routeId) {

        return routeRepository.findById(routeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Route not found with ID: "
                                        + routeId
                        )
                );
    }

    /**
     * Find stop or throw 404.
     */
    private Stop findStopById(UUID stopId) {

        return stopRepository.findById(stopId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Stop not found with ID: "
                                        + stopId
                        )
                );
    }

    /**
     * Convert Stop entity into StopResponse DTO.
     */
    private StopResponse mapToResponse(Stop stop) {

        return new StopResponse(
                stop.getId(),
                stop.getRoute().getId(),
                stop.getStopName(),
                stop.getAddress(),
                stop.getLatitude(),
                stop.getLongitude(),
                stop.getStopOrder(),
                stop.getEstimatedArrivalOffsetMinutes(),
                stop.getStatus(),
                stop.getActive(),
                stop.getCreatedAt(),
                stop.getUpdatedAt()
        );
    }
}