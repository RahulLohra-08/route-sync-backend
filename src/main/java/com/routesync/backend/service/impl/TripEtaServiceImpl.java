
package com.routesync.backend.service.impl;

import com.routesync.backend.dto.trip.location.TripEtaResponse;
import com.routesync.backend.entity.Stop;
import com.routesync.backend.entity.Trip;
import com.routesync.backend.entity.TripLocation;
import com.routesync.backend.entity.enums.TripStatus;
import com.routesync.backend.exception.ResourceNotFoundException;
import com.routesync.backend.repository.StopRepository;
import com.routesync.backend.repository.TripLocationRepository;
import com.routesync.backend.repository.TripRepository;
import com.routesync.backend.service.TripEtaService;
import com.routesync.backend.service.routing.RoadRoutingService;
import com.routesync.backend.service.routing.RouteCoordinate;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripEtaServiceImpl implements TripEtaService {

    private final TripRepository tripRepository;
    private final TripLocationRepository tripLocationRepository;
    private final StopRepository stopRepository;
    private final RoadRoutingService roadRoutingService;

    @Value("${routesync.tracking.location-freshness-seconds:30}")
    private long freshnessThresholdSeconds;

    @Override
    public TripEtaResponse calculateEta(UUID tripId, UUID stopId) {

        // Henglish: Pehle active trip find karo.
        Trip trip = tripRepository.findByIdAndActiveTrue(tripId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active trip not found: " + tripId
                ));

        // Henglish: Route ke active stops stopOrder ke according lao.
        List<Stop> routeStops =
                stopRepository.findAllByRouteIdAndActiveTrueOrderByStopOrderAsc(
                        trip.getRoute().getId()
                );

        if (routeStops.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No active stops found for route: "
                            + trip.getRoute().getId()
            );
        }

        // Henglish: Target stop isi route par hona chahiye.
        int targetIndex = -1;

        for (int i = 0; i < routeStops.size(); i++) {
            if (routeStops.get(i).getId().equals(stopId)) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex == -1) {
            throw new ResourceNotFoundException(
                    "Active stop not found on this trip's route: " + stopId
            );
        }

        Stop targetStop = routeStops.get(targetIndex);

        // Henglish: Latest GPS record fetch karo.
        TripLocation location = tripLocationRepository
                .findFirstByTripIdOrderByRecordedAtDesc(tripId)
                .orElse(null);

        if (location == null) {
            return unavailable(
                    tripId, targetStop, false, false,
                    "No GPS location is available for this trip."
            );
        }

        LocalDateTime recordedAt = location.getRecordedAt();

        // Henglish: Missing timestamp se freshness calculate nahi kar sakte.
        if (recordedAt == null) {
            return unavailable(
                    tripId, targetStop, true, false,
                    "GPS timestamp is missing."
            );
        }

        LocalDateTime now = LocalDateTime.now();

        long ageMillis = Duration.between(recordedAt, now).toMillis();

        boolean locationFresh =
                ageMillis >= 0
                        && ageMillis <= freshnessThresholdSeconds * 1000L;

        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            return unavailable(
                    tripId, targetStop, true, locationFresh,
                    "ETA is available only while the trip is in progress."
            );
        }

        if (!locationFresh) {
            return unavailable(
                    tripId, targetStop, true, false,
                    "GPS location is stale or has an invalid timestamp."
            );
        }

        // Henglish: Current GPS aur target stop coordinates validate karo.
        if (!validCoordinates(
                location.getLatitude(), location.getLongitude())
                || !validCoordinates(
                targetStop.getLatitude(), targetStop.getLongitude())) {

            return unavailable(
                    tripId, targetStop, true, true,
                    "Current GPS or target stop coordinates are invalid."
            );
        }

        // Henglish: Bus ke nearest valid stop ka index find karo.
        int nearestStopIndex = findNearestStopIndex(
                location.getLatitude(),
                location.getLongitude(),
                routeStops
        );

        if (nearestStopIndex == -1) {
            return unavailable(
                    tripId, targetStop, true, true,
                    "Could not determine route progress from available stop coordinates."
            );
        }

        // Henglish: Nearest-stop estimation ke according passed stop reject karo.
        if (targetIndex < nearestStopIndex) {
            return unavailable(
                    tripId, targetStop, true, true,
                    "Target stop appears to be behind the bus based on nearest-stop estimation."
            );
        }

        /*
         * Henglish:
         * Step 1 - Existing v2 geographic distance calculate karo.
         * Step 2 - OSRM road distance try karo.
         * Step 3 - OSRM fail hone par v2 distance fallback use karo.
         */

        double geographicDistanceKm = 0.0;

        if (targetIndex == nearestStopIndex) {

            // Henglish: Target stop nearest stop hai, direct GPS distance lo.
            geographicDistanceKm = haversineKm(
                    location.getLatitude(),
                    location.getLongitude(),
                    targetStop.getLatitude(),
                    targetStop.getLongitude()
            );

        } else {

            // Henglish: Current GPS se nearest stop tak distance.
            Stop nearestStop = routeStops.get(nearestStopIndex);

            geographicDistanceKm += haversineKm(
                    location.getLatitude(),
                    location.getLongitude(),
                    nearestStop.getLatitude(),
                    nearestStop.getLongitude()
            );

            // Henglish: Ordered route segments ka geographic distance add karo.
            for (int i = nearestStopIndex; i < targetIndex; i++) {

                Stop currentStop = routeStops.get(i);
                Stop nextStop = routeStops.get(i + 1);

                if (!validCoordinates(
                        currentStop.getLatitude(),
                        currentStop.getLongitude())
                        || !validCoordinates(
                        nextStop.getLatitude(),
                        nextStop.getLongitude())) {

                    return unavailable(
                            tripId, targetStop, true, true,
                            "A stop on the remaining route has invalid coordinates."
                    );
                }

                geographicDistanceKm += haversineKm(
                        currentStop.getLatitude(),
                        currentStop.getLongitude(),
                        nextStop.getLatitude(),
                        nextStop.getLongitude()
                );
            }
        }

        // Henglish: Bus GPS aur remaining ordered stops se road distance try karo.
        Optional<Double> roadDistance = calculateRoadDistanceKm(
                location.getLatitude(),
                location.getLongitude(),
                routeStops,
                nearestStopIndex,
                targetIndex
        );

        double remainingDistanceKm;
        String calculationMethod;
        String etaMessage;

        if (roadDistance.isPresent()) {

            remainingDistanceKm = roadDistance.get();

            calculationMethod =
                    "OSRM_ROAD_DISTANCE_AND_CURRENT_SPEED";

            etaMessage =
                    "Approximate ETA using OSRM road distance and current GPS speed. "
                            + "Traffic and stop dwell times are not included.";

        } else {

            // Henglish: Routing fail hua toh existing v2 result use karo.
            remainingDistanceKm = geographicDistanceKm;

            calculationMethod =
                    "ORDERED_STOPS_GEOGRAPHIC_DISTANCE_AND_CURRENT_SPEED";

            etaMessage =
                    "Approximate ETA using ordered stop coordinates and current GPS speed. "
                            + "Road geometry, traffic, and stop dwell times are not included.";
        }

        // Henglish: Entity contract ke according speed km/h mein hai.
        Double speedKmh = location.getSpeed();

        if (speedKmh == null
                || !Double.isFinite(speedKmh)
                || speedKmh < 0) {

            return unavailableWithDistance(
                    tripId, targetStop, remainingDistanceKm,
                    true, true,
                    "GPS speed is missing or invalid."
            );
        }

        // Henglish: Low speed par current-speed ETA reliable nahi hai.
        if (speedKmh < 3.0) {
            return unavailableWithDistance(
                    tripId, targetStop, remainingDistanceKm,
                    true, true,
                    "Bus speed is too low for a reliable ETA estimate."
            );
        }

        // Henglish: Distance km aur speed km/h hai, isliye ETA minutes mein.
        double etaMinutes = (remainingDistanceKm / speedKmh) * 60.0;

        LocalDateTime estimatedArrivalAt =
                now.plusSeconds(Math.round(etaMinutes * 60.0));

        return new TripEtaResponse(
                tripId,
                targetStop.getId(),
                targetStop.getStopName(),
                round(remainingDistanceKm, 2),
                round(etaMinutes, 1),
                estimatedArrivalAt,
                true,
                true,
                calculationMethod,
                etaMessage
        );
    }

    /**
     * Henglish: OSRM se remaining road distance calculate karo.
     * Routing unavailable ho toh Optional.empty() return hoga.
     */
    private Optional<Double> calculateRoadDistanceKm(
            Double busLatitude,
            Double busLongitude,
            List<Stop> orderedStops,
            int startIndex,
            int targetIndex
    ) {
        if (!validCoordinates(busLatitude, busLongitude)
                || orderedStops == null
                || startIndex < 0
                || targetIndex < 0
                || targetIndex >= orderedStops.size()
                || startIndex > targetIndex) {

            return Optional.empty();
        }

        List<RouteCoordinate> coordinates = new ArrayList<>();

        // Henglish: First waypoint bus ki current GPS location hai.
        coordinates.add(new RouteCoordinate(
                busLatitude,
                busLongitude
        ));

        // Henglish: Nearest stop se target stop tak ordered waypoints add karo.
        for (int i = startIndex; i <= targetIndex; i++) {

            Stop stop = orderedStops.get(i);

            if (!validCoordinates(
                    stop.getLatitude(),
                    stop.getLongitude())) {
                return Optional.empty();
            }

            coordinates.add(new RouteCoordinate(
                    stop.getLatitude(),
                    stop.getLongitude()
            ));
        }

        // Henglish: Routing implementation road distance ya empty result dega.
        return roadRoutingService.calculateRoadDistanceKm(coordinates);
    }

    private int findNearestStopIndex(
            double latitude,
            double longitude,
            List<Stop> routeStops
    ) {
        int nearestIndex = -1;
        double minimumDistanceKm = Double.MAX_VALUE;

        for (int i = 0; i < routeStops.size(); i++) {

            Stop stop = routeStops.get(i);

            if (!validCoordinates(
                    stop.getLatitude(), stop.getLongitude())) {
                continue;
            }

            double distanceKm = haversineKm(
                    latitude,
                    longitude,
                    stop.getLatitude(),
                    stop.getLongitude()
            );

            if (distanceKm < minimumDistanceKm) {
                minimumDistanceKm = distanceKm;
                nearestIndex = i;
            }
        }

        return nearestIndex;
    }

    private boolean validCoordinates(
            Double latitude,
            Double longitude
    ) {
        return latitude != null
                && longitude != null
                && Double.isFinite(latitude)
                && Double.isFinite(longitude)
                && latitude >= -90.0
                && latitude <= 90.0
                && longitude >= -180.0
                && longitude <= 180.0;
    }

    private TripEtaResponse unavailable(
            UUID tripId,
            Stop stop,
            boolean locationAvailable,
            boolean locationFresh,
            String message
    ) {
        return new TripEtaResponse(
                tripId,
                stop.getId(),
                stop.getStopName(),
                null,
                null,
                null,
                locationAvailable,
                locationFresh,
                "UNAVAILABLE",
                message
        );
    }

    private TripEtaResponse unavailableWithDistance(
            UUID tripId,
            Stop stop,
            double distanceKm,
            boolean locationAvailable,
            boolean locationFresh,
            String message
    ) {
        return new TripEtaResponse(
                tripId,
                stop.getId(),
                stop.getStopName(),
                round(distanceKm, 2),
                null,
                null,
                locationAvailable,
                locationFresh,
                "UNAVAILABLE",
                message
        );
    }

    // Henglish: Earth par do coordinates ke beech straight-line distance.
    private double haversineKm(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {
        double earthRadiusKm = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double safeA = Math.max(0.0, Math.min(1.0, a));

        return earthRadiusKm * 2 * Math.atan2(
                Math.sqrt(safeA),
                Math.sqrt(1.0 - safeA)
        );
    }

    private double round(double value, int decimalPlaces) {
        double factor = Math.pow(10, decimalPlaces);
        return Math.round(value * factor) / factor;
    }
}
