package com.routesync.backend.service.impl;

import com.routesync.backend.websocket.TripLocationWebSocketMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.routesync.backend.dto.trip.location.TripLocationResponse;
import com.routesync.backend.dto.trip.location.UpdateTripLocationRequest;
import com.routesync.backend.entity.Driver;
import com.routesync.backend.entity.Trip;
import com.routesync.backend.entity.TripLocation;
import com.routesync.backend.entity.enums.TripStatus;
import com.routesync.backend.exception.BadRequestException;
import com.routesync.backend.exception.ResourceNotFoundException;
import com.routesync.backend.repository.DriverRepository;
import com.routesync.backend.repository.TripLocationRepository;
import com.routesync.backend.repository.TripRepository;
import com.routesync.backend.service.TripLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TripLocationServiceImpl implements TripLocationService {

    private final TripLocationRepository tripLocationRepository;
    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;
    private final SimpMessagingTemplate messagingTemplate;


    // =========================================================
    // UPDATE CURRENT LOCATION
    // =========================================================

    @Override
    public TripLocationResponse updateLocation(
            UUID tripId,
            UUID driverUserId,
            UpdateTripLocationRequest request
    ) {

        // -----------------------------------------------------
        // 1. Find the authenticated driver's profile
        // -----------------------------------------------------

        Driver driver = driverRepository
                .findByUserIdAndActiveTrue(driverUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active driver profile not found for user id: "
                                        + driverUserId
                        )
                );


        // -----------------------------------------------------
        // 2. Find active trip
        // -----------------------------------------------------

        Trip trip = tripRepository
                .findByIdAndActiveTrue(tripId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active trip not found with id: "
                                        + tripId
                        )
                );


        // -----------------------------------------------------
        // 3. Verify trip belongs to this driver
        // -----------------------------------------------------

        if (!trip.getDriver().getId().equals(driver.getId())) {

            throw new BadRequestException(
                    "This trip is not assigned to the current driver"
            );
        }


        // -----------------------------------------------------
        // 4. GPS updates are allowed only during trip
        // -----------------------------------------------------

        if (trip.getStatus() != TripStatus.IN_PROGRESS) {

            throw new BadRequestException(
                    "GPS location can only be updated for an IN_PROGRESS trip"
            );
        }


        // -----------------------------------------------------
        // 5. Validate GPS values
        // -----------------------------------------------------

        validateLocation(request);


        // -----------------------------------------------------
        // 6. Create GPS history record
        // -----------------------------------------------------

        TripLocation location = TripLocation.builder()
                .trip(trip)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .speed(request.speed())
                .heading(request.heading())
                .accuracy(request.accuracy())
                .recordedAt(LocalDateTime.now())
                .build();


        TripLocation savedLocation =
                tripLocationRepository.save(location);


        // -----------------------------------------------------
        // 7. Update latest GPS snapshot on Trip
        // -----------------------------------------------------

        trip.setCurrentLatitude(request.latitude());
        trip.setCurrentLongitude(request.longitude());
        trip.setLastLocationUpdate(savedLocation.getRecordedAt());

        tripRepository.save(trip);


        TripLocationWebSocketMessage message =
                new TripLocationWebSocketMessage(
                        trip.getId(),
                        trip.getBus().getId(),
                        trip.getBus().getBusNumber(),
                        trip.getRoute().getId(),
                        trip.getRoute().getRouteCode(),
                        savedLocation.getLatitude(),
                        savedLocation.getLongitude(),
                        savedLocation.getSpeed(),
                        savedLocation.getHeading(),
                        savedLocation.getAccuracy(),
                        savedLocation.getRecordedAt()
                );

        messagingTemplate.convertAndSend(
                "/topic/trips/" + trip.getId() + "/location",
                message
        );

        // -----------------------------------------------------
        // 8. Return response
        // -----------------------------------------------------

        return mapToResponse(savedLocation);
    }


    // =========================================================
    // GET LOCATION HISTORY
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<TripLocationResponse> getTripLocationHistory(
            UUID tripId
    ) {

        // Make sure trip exists.
        tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Trip not found with id: " + tripId
                        )
                );

        return tripLocationRepository
                .findAllByTripIdOrderByRecordedAtAsc(tripId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // GET LATEST LOCATION
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public TripLocationResponse getLatestLocation(
            UUID tripId
    ) {

        // Make sure trip exists.
        tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Trip not found with id: " + tripId
                        )
                );

        return tripLocationRepository
                .findFirstByTripIdOrderByRecordedAtDesc(tripId)
                .map(this::mapToResponse)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No GPS location found for trip: "
                                        + tripId
                        )
                );
    }

    private Trip getDriverTrip(
            UUID tripId,
            UUID driverUserId
    ) {

        Driver driver = driverRepository
                .findByUserIdAndActiveTrue(driverUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active driver profile not found for user id: "
                                        + driverUserId
                        )
                );

        Trip trip = tripRepository
                .findByIdAndActiveTrue(tripId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active trip not found with id: "
                                        + tripId
                        )
                );

        if (!trip.getDriver().getId().equals(driver.getId())) {

            throw new BadRequestException(
                    "This trip is not assigned to the current driver"
            );
        }

        return trip;
    }

//    ---------------------- Driver trip location history-------------//
@Override
@Transactional(readOnly = true)
public List<TripLocationResponse> getDriverTripLocationHistory(
        UUID tripId,
        UUID driverUserId
) {

    Trip trip = getDriverTrip(
            tripId,
            driverUserId
    );

    return tripLocationRepository
            .findAllByTripIdOrderByRecordedAtAsc(
                    trip.getId()
            )
            .stream()
            .map(this::mapToResponse)
            .toList();
}

//---------------- Driver latest location --------------------//
@Override
@Transactional(readOnly = true)
public TripLocationResponse getDriverLatestLocation(
        UUID tripId,
        UUID driverUserId
) {

    Trip trip = getDriverTrip(
            tripId,
            driverUserId
    );

    return tripLocationRepository
            .findAllByTripIdOrderByRecordedAtDesc(
                    trip.getId()
            )
            .stream()
            .findFirst()
            .map(this::mapToResponse)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "No GPS location found for trip: "
                                    + tripId
                    )
            );
}


    // =========================================================
    // GPS VALIDATION
    // =========================================================

    private void validateLocation(
            UpdateTripLocationRequest request
    ) {

        if (request.latitude() < -90
                || request.latitude() > 90) {

            throw new BadRequestException(
                    "Latitude must be between -90 and 90"
            );
        }


        if (request.longitude() < -180
                || request.longitude() > 180) {

            throw new BadRequestException(
                    "Longitude must be between -180 and 180"
            );
        }


        if (request.speed() != null
                && request.speed() < 0) {

            throw new BadRequestException(
                    "Speed cannot be negative"
            );
        }


        if (request.heading() != null
                && (request.heading() < 0
                || request.heading() >= 360)) {

            throw new BadRequestException(
                    "Heading must be between 0 and 359.999999"
            );
        }


        if (request.accuracy() != null
                && request.accuracy() < 0) {

            throw new BadRequestException(
                    "GPS accuracy cannot be negative"
            );
        }
    }


    // =========================================================
    // ENTITY → DTO
    // =========================================================

    private TripLocationResponse mapToResponse(
            TripLocation location
    ) {

        return new TripLocationResponse(
                location.getId(),
                location.getTrip().getId(),
                location.getLatitude(),
                location.getLongitude(),
                location.getSpeed(),
                location.getHeading(),
                location.getAccuracy(),
                location.getRecordedAt()
        );
    }
}