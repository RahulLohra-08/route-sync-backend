
package com.routesync.backend.service.impl;

import com.routesync.backend.dto.trip.location.TripTrackingSnapshotResponse;
import com.routesync.backend.entity.Trip;
import com.routesync.backend.entity.TripLocation;
import com.routesync.backend.exception.ResourceNotFoundException;
import com.routesync.backend.repository.TripLocationRepository;
import com.routesync.backend.repository.TripRepository;
import com.routesync.backend.service.TripTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripTrackingServiceImpl implements TripTrackingService {

    private final TripRepository tripRepository;
    private final TripLocationRepository tripLocationRepository;

    @Value("${routesync.tracking.location-freshness-seconds:30}")
    private long freshnessThresholdSeconds;

    @Override
    public TripTrackingSnapshotResponse getTrackingSnapshot(UUID tripId) {

        // Henglish: Sirf active trip ka snapshot return karenge.
        Trip trip = tripRepository.findByIdAndActiveTrue(tripId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active trip not found: " + tripId
                ));

        // Henglish: Latest GPS record directly database se fetch karo.
        // Puri location history memory mein load nahi hogi.
        TripLocation location = tripLocationRepository
                .findFirstByTripIdOrderByRecordedAtDesc(tripId)
                .orElse(null);

        if (location == null) {
            return new TripTrackingSnapshotResponse(
                    trip.getId(),
                    trip.getStatus(),
                    trip.getBus().getId(),
                    trip.getBus().getBusNumber(),
                    trip.getRoute().getId(),
                    trip.getRoute().getRouteCode(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    false
            );
        }

        LocalDateTime recordedAt = location.getRecordedAt();
        LocalDateTime now = LocalDateTime.now();

        // Henglish: Future timestamp ko fresh location nahi maanenge.
        long ageSeconds = Duration.between(recordedAt, now).getSeconds();

        boolean locationFresh =
                ageSeconds >= 0
                        && ageSeconds <= freshnessThresholdSeconds;

        return new TripTrackingSnapshotResponse(
                trip.getId(),
                trip.getStatus(),
                trip.getBus().getId(),
                trip.getBus().getBusNumber(),
                trip.getRoute().getId(),
                trip.getRoute().getRouteCode(),
                location.getLatitude(),
                location.getLongitude(),
                location.getSpeed(),
                location.getHeading(),
                location.getAccuracy(),
                recordedAt,
                true,
                locationFresh
        );
    }
}
