package com.routesync.backend.service.impl;

import com.routesync.backend.dto.trip.CreateTripRequest;
import com.routesync.backend.dto.trip.TripResponse;
import com.routesync.backend.dto.trip.UpdateTripRequest;
import com.routesync.backend.entity.Bus;
import com.routesync.backend.entity.Driver;
import com.routesync.backend.entity.Route;
import com.routesync.backend.entity.Trip;
import com.routesync.backend.entity.enums.TripStatus;
import com.routesync.backend.exception.BadRequestException;
import com.routesync.backend.exception.ResourceNotFoundException;
import com.routesync.backend.repository.BusRepository;
import com.routesync.backend.repository.DriverRepository;
import com.routesync.backend.repository.RouteRepository;
import com.routesync.backend.repository.TripRepository;
import com.routesync.backend.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final DriverRepository driverRepository;

    /**
     * Create a new Trip.
     *
     * Henglish:
     * Yahan hum check karenge ki Route, Bus aur Driver valid hain ya nahi.
     * Saath hi Bus aur Driver ka same time par koi dusra Trip
     * already scheduled hai ya nahi.
     */
    @Override
    public TripResponse createTrip(CreateTripRequest request) {

        // ---------------------------------------------------------
        // 1. Scheduled time validate karo
        // ---------------------------------------------------------
        validateSchedule(
                request.scheduledStartTime(),
                request.scheduledEndTime()
        );

        // ---------------------------------------------------------
        // 2. Route find karo
        // Route exist bhi hona chahiye aur active bhi.
        // ---------------------------------------------------------
        Route route = routeRepository.findById(request.routeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Route not found with id: "
                                        + request.routeId()
                        )
                );

        if (!Boolean.TRUE.equals(route.getActive())) {
            throw new BadRequestException(
                    "Cannot create trip for an inactive route"
            );
        }

        // ---------------------------------------------------------
        // 3. Bus find karo
        // Bus bhi active hona chahiye.
        // ---------------------------------------------------------
        Bus bus = busRepository.findById(request.busId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bus not found with id: "
                                        + request.busId()
                        )
                );

        if (!Boolean.TRUE.equals(bus.getActive())) {
            throw new BadRequestException(
                    "Cannot create trip for an inactive bus"
            );
        }

        // ---------------------------------------------------------
        // 4. Driver find karo
        // Driver bhi active hona chahiye.
        // ---------------------------------------------------------
        Driver driver = driverRepository.findById(request.driverId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver not found with id: "
                                        + request.driverId()
                        )
                );

        if (!Boolean.TRUE.equals(driver.getActive())) {
            throw new BadRequestException(
                    "Cannot create trip for an inactive driver"
            );
        }

        // ---------------------------------------------------------
        // 5. Bus capacity validate karo
        //
        // Trip.totalSeats ko client se nahi lenge.
        // Bus ki actual capacity ko snapshot karenge.
        // ---------------------------------------------------------
        if (bus.getCapacity() == null || bus.getCapacity() <= 0) {
            throw new BadRequestException(
                    "Bus capacity must be greater than zero"
            );
        }

        // ---------------------------------------------------------
        // 6. Check karo Bus already kisi overlapping Trip mein
        // assigned toh nahi hai.
        // ---------------------------------------------------------
        validateBusAvailability(
                bus.getId(),
                request.scheduledStartTime(),
                request.scheduledEndTime()
        );

        // ---------------------------------------------------------
        // 7. Check karo Driver already kisi overlapping Trip mein
        // assigned toh nahi hai.
        // ---------------------------------------------------------
        validateDriverAvailability(
                driver.getId(),
                request.scheduledStartTime(),
                request.scheduledEndTime()
        );

        // ---------------------------------------------------------
        // 8. Trip entity create karo
        // ---------------------------------------------------------
        Trip trip = new Trip();

        trip.setRoute(route);
        trip.setBus(bus);
        trip.setDriver(driver);

        trip.setScheduledStartTime(
                request.scheduledStartTime()
        );

        trip.setScheduledEndTime(
                request.scheduledEndTime()
        );

        // ---------------------------------------------------------
        // Bus ki capacity ko Trip ke liye snapshot kar rahe hain.
        //
        // Example:
        // Bus capacity = 50
        // Trip totalSeats = 50
        //
        // Future mein Bus capacity change ho bhi jaye,
        // purane Trip ka data correct rahega.
        // ---------------------------------------------------------
        trip.setTotalSeats(bus.getCapacity());

        // Trip create hote time koi passenger onboard nahi hai.
        trip.setCurrentOccupancy(0);

        // New Trip ka initial status.
        trip.setStatus(TripStatus.SCHEDULED);

        // Soft-delete/active flag.
        trip.setActive(true);

        // ---------------------------------------------------------
        // 9. Trip database mein save karo
        // ---------------------------------------------------------
        Trip savedTrip = tripRepository.save(trip);

        // ---------------------------------------------------------
        // 10. Entity ko API Response DTO mein convert karo
        // ---------------------------------------------------------
        return mapToResponse(savedTrip);
    }

    /**
     * Get Trip by ID.
     *
     * Henglish:
     * Sirf active Trip return karenge.
     */
    @Override
    @Transactional(readOnly = true)
    public TripResponse getTripById(UUID tripId) {

        Trip trip = findActiveTrip(tripId);

        return mapToResponse(trip);
    }

    /**
     * Get all active Trips.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getAllActiveTrips() {

        return tripRepository.findAllByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get Trips by Route.
     *
     * Henglish:
     * Isse Passenger app mein kisi particular route ke
     * available trips dikha sakte hain.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getTripsByRoute(UUID routeId) {

        return tripRepository
                .findAllByRouteIdAndActiveTrue(routeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get Trips by Bus.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getTripsByBus(UUID busId) {

        return tripRepository
                .findAllByBusIdAndActiveTrue(busId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get Trips by Driver.
     *
     * Henglish:
     * Driver mobile app mein driver ke assigned Trips
     * retrieve karne ke liye useful hoga.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getTripsByDriver(UUID driverId) {

        return tripRepository
                .findAllByDriverIdAndActiveTrue(driverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Update Trip configuration.
     */
    @Override
    public TripResponse updateTrip(
            UUID tripId,
            UpdateTripRequest request
    ) {

        Trip trip = findActiveTrip(tripId);

        // ---------------------------------------------------------
        // Completed/Cancelled Trip ko normal update nahi karna.
        // ---------------------------------------------------------
        if (trip.getStatus() == TripStatus.COMPLETED) {
            throw new BadRequestException(
                    "Completed trip cannot be updated"
            );
        }

        if (trip.getStatus() == TripStatus.CANCELLED) {
            throw new BadRequestException(
                    "Cancelled trip cannot be updated"
            );
        }

        // ---------------------------------------------------------
        // Existing values use karo agar request mein new value
        // nahi di gayi hai.
        // ---------------------------------------------------------
        LocalDateTime scheduledStart =
                request.scheduledStartTime() != null
                        ? request.scheduledStartTime()
                        : trip.getScheduledStartTime();

        LocalDateTime scheduledEnd =
                request.scheduledEndTime() != null
                        ? request.scheduledEndTime()
                        : trip.getScheduledEndTime();

        // ---------------------------------------------------------
        // New schedule validate karo.
        // ---------------------------------------------------------
        validateSchedule(scheduledStart, scheduledEnd);

        // ---------------------------------------------------------
        // Route change
        // ---------------------------------------------------------
        if (request.routeId() != null
                && !request.routeId().equals(
                trip.getRoute().getId()
        )) {

            Route route = routeRepository
                    .findById(request.routeId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Route not found with id: "
                                            + request.routeId()
                            )
                    );

            if (!Boolean.TRUE.equals(route.getActive())) {
                throw new BadRequestException(
                        "Cannot assign an inactive route"
                );
            }

            trip.setRoute(route);
        }

        // ---------------------------------------------------------
        // Bus change
        // ---------------------------------------------------------
        if (request.busId() != null
                && !request.busId().equals(
                trip.getBus().getId()
        )) {

            Bus bus = busRepository
                    .findById(request.busId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Bus not found with id: "
                                            + request.busId()
                            )
                    );

            if (!Boolean.TRUE.equals(bus.getActive())) {
                throw new BadRequestException(
                        "Cannot assign an inactive bus"
                );
            }

            if (bus.getCapacity() == null
                    || bus.getCapacity() <= 0) {

                throw new BadRequestException(
                        "Bus capacity must be greater than zero"
                );
            }

            // New Bus ka schedule conflict check.
            validateBusAvailabilityForUpdate(
                    bus.getId(),
                    trip.getId(),
                    scheduledStart,
                    scheduledEnd
            );

            trip.setBus(bus);

            // New Bus ki capacity ko snapshot karo.
            trip.setTotalSeats(bus.getCapacity());

            // -----------------------------------------------------
            // Important:
            // Existing occupancy new bus ki capacity se zyada
            // nahi honi chahiye.
            // -----------------------------------------------------
            if (trip.getCurrentOccupancy() > bus.getCapacity()) {
                throw new BadRequestException(
                        "Current occupancy exceeds new bus capacity"
                );
            }
        }

        // ---------------------------------------------------------
        // Driver change
        // ---------------------------------------------------------
        if (request.driverId() != null
                && !request.driverId().equals(
                trip.getDriver().getId()
        )) {

            Driver driver = driverRepository
                    .findById(request.driverId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Driver not found with id: "
                                            + request.driverId()
                            )
                    );

            if (!Boolean.TRUE.equals(driver.getActive())) {
                throw new BadRequestException(
                        "Cannot assign an inactive driver"
                );
            }

            // New Driver ka schedule conflict check.
            validateDriverAvailabilityForUpdate(
                    driver.getId(),
                    trip.getId(),
                    scheduledStart,
                    scheduledEnd
            );

            trip.setDriver(driver);
        }

        // ---------------------------------------------------------
        // Schedule update
        // ---------------------------------------------------------
        trip.setScheduledStartTime(scheduledStart);
        trip.setScheduledEndTime(scheduledEnd);

        // ---------------------------------------------------------
        // Status update
        // ---------------------------------------------------------
        if (request.status() != null) {

            validateStatusTransition(
                    trip.getStatus(),
                    request.status()
            );

            trip.setStatus(request.status());
        }

        // ---------------------------------------------------------
        // Occupancy update
        // ---------------------------------------------------------
        if (request.currentOccupancy() != null) {

            validateOccupancy(
                    request.currentOccupancy(),
                    trip.getTotalSeats()
            );

            trip.setCurrentOccupancy(
                    request.currentOccupancy()
            );
        }

        Trip updatedTrip = tripRepository.save(trip);

        return mapToResponse(updatedTrip);
    }

    /**
     * Soft-delete / deactivate Trip.
     *
     * Henglish:
     * Database se record delete nahi karenge.
     * Sirf active=false karenge.
     */
    @Override
    public void deactivateTrip(UUID tripId) {

        Trip trip = findActiveTrip(tripId);

        if (trip.getStatus() == TripStatus.IN_PROGRESS) {
            throw new BadRequestException(
                    "In-progress trip cannot be deactivated"
            );
        }

        trip.setActive(false);

        tripRepository.save(trip);
    }

    /**
     * Start Trip.
     *
     * SCHEDULED/BOARDING → IN_PROGRESS
     */
    @Override
    public TripResponse startTrip(UUID tripId) {

        Trip trip = findActiveTrip(tripId);

        if (trip.getStatus() != TripStatus.SCHEDULED
                && trip.getStatus() != TripStatus.BOARDING) {

            throw new BadRequestException(
                    "Trip cannot be started from status: "
                            + trip.getStatus()
            );
        }

        trip.setStatus(TripStatus.IN_PROGRESS);

        // Actual start time store karo.
        trip.setActualStartTime(LocalDateTime.now());

        Trip updatedTrip = tripRepository.save(trip);

        return mapToResponse(updatedTrip);
    }

    /**
     * Complete Trip.
     *
     * IN_PROGRESS → COMPLETED
     */
    @Override
    public TripResponse completeTrip(UUID tripId) {

        Trip trip = findActiveTrip(tripId);

        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new BadRequestException(
                    "Only an in-progress trip can be completed"
            );
        }

        trip.setStatus(TripStatus.COMPLETED);

        // Actual completion time store karo.
        trip.setActualEndTime(LocalDateTime.now());

        Trip updatedTrip = tripRepository.save(trip);

        return mapToResponse(updatedTrip);
    }

    /**
     * Cancel Trip.
     */
    @Override
    public TripResponse cancelTrip(UUID tripId) {

        Trip trip = findActiveTrip(tripId);

        if (trip.getStatus() == TripStatus.COMPLETED) {
            throw new BadRequestException(
                    "Completed trip cannot be cancelled"
            );
        }

        if (trip.getStatus() == TripStatus.CANCELLED) {
            throw new BadRequestException(
                    "Trip is already cancelled"
            );
        }

        trip.setStatus(TripStatus.CANCELLED);

        Trip updatedTrip = tripRepository.save(trip);

        return mapToResponse(updatedTrip);
    }

    // =========================================================
    // PRIVATE HELPER METHODS
    // =========================================================

    /**
     * Active Trip find karne ka common method.
     *
     * Henglish:
     * Baar-baar same repository code likhne ke bajaye
     * ek reusable method bana diya.
     */
    private Trip findActiveTrip(UUID tripId) {

        return tripRepository.findByIdAndActiveTrue(tripId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active trip not found with id: "
                                        + tripId
                        )
                );
    }

    /**
     * Scheduled start/end time validate karta hai.
     */
    private void validateSchedule(
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd
    ) {

        if (scheduledStart == null || scheduledEnd == null) {
            throw new BadRequestException(
                    "Scheduled start and end time are required"
            );
        }

        if (!scheduledEnd.isAfter(scheduledStart)) {
            throw new BadRequestException(
                    "Scheduled end time must be after scheduled start time"
            );
        }
    }

    /**
     * Bus availability check.
     */
    private void validateBusAvailability(
            UUID busId,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd
    ) {

        boolean hasOverlap =
                !tripRepository.findOverlappingTripsForBus(
                        busId,
                        scheduledStart,
                        scheduledEnd
                ).isEmpty();

        if (hasOverlap) {
            throw new BadRequestException(
                    "Bus is already assigned to another trip during "
                            + "the selected time"
            );
        }
    }

    /**
     * Driver availability check.
     */
    private void validateDriverAvailability(
            UUID driverId,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd
    ) {

        boolean hasOverlap =
                !tripRepository.findOverlappingTripsForDriver(
                        driverId,
                        scheduledStart,
                        scheduledEnd
                ).isEmpty();

        if (hasOverlap) {
            throw new BadRequestException(
                    "Driver is already assigned to another trip during "
                            + "the selected time"
            );
        }
    }

    /**
     * Update ke time Bus conflict check.
     *
     * Current Trip ko ignore karte hain.
     */
    private void validateBusAvailabilityForUpdate(
            UUID busId,
            UUID tripId,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd
    ) {

        boolean hasOverlap =
                !tripRepository
                        .findOverlappingTripsForBusExcludingTrip(
                                busId,
                                tripId,
                                scheduledStart,
                                scheduledEnd
                        )
                        .isEmpty();

        if (hasOverlap) {
            throw new BadRequestException(
                    "Bus is already assigned to another trip "
                            + "during the selected time"
            );
        }
    }

    /**
     * Update ke time Driver conflict check.
     */
    private void validateDriverAvailabilityForUpdate(
            UUID driverId,
            UUID tripId,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd
    ) {

        boolean hasOverlap =
                !tripRepository
                        .findOverlappingTripsForDriverExcludingTrip(
                                driverId,
                                tripId,
                                scheduledStart,
                                scheduledEnd
                        )
                        .isEmpty();

        if (hasOverlap) {
            throw new BadRequestException(
                    "Driver is already assigned to another trip "
                            + "during the selected time"
            );
        }
    }

    /**
     * Occupancy validation.
     *
     * Example:
     * totalSeats = 50
     * occupancy = 35  -> valid
     * occupancy = 55  -> invalid
     */
    private void validateOccupancy(
            Integer occupancy,
            Integer totalSeats
    ) {

        if (occupancy == null || occupancy < 0) {
            throw new BadRequestException(
                    "Current occupancy cannot be negative"
            );
        }

        if (totalSeats == null || totalSeats <= 0) {
            throw new BadRequestException(
                    "Trip total seats must be greater than zero"
            );
        }

        if (occupancy > totalSeats) {
            throw new BadRequestException(
                    "Current occupancy cannot exceed total seats"
            );
        }
    }

    /**
     * Trip status transition validate karta hai.
     *
     * Henglish:
     * Har status se har status par jump allow nahi karenge.
     */
    private void validateStatusTransition(
            TripStatus currentStatus,
            TripStatus newStatus
    ) {

        if (currentStatus == newStatus) {
            return;
        }

        boolean valid = switch (currentStatus) {

            case SCHEDULED ->
                    newStatus == TripStatus.BOARDING
                            || newStatus == TripStatus.CANCELLED;

            case BOARDING ->
                    newStatus == TripStatus.IN_PROGRESS
                            || newStatus == TripStatus.CANCELLED;

            case IN_PROGRESS ->
                    newStatus == TripStatus.COMPLETED
                            || newStatus == TripStatus.CANCELLED;

            case COMPLETED, CANCELLED ->
                    false;
        };

        if (!valid) {
            throw new BadRequestException(
                    "Invalid trip status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }
    }

//   --------------------Driver ---------------------//


//    userId → Driver → driverId → Trips

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getDriverTrips(UUID driverUserId) {

        Driver driver = driverRepository
                .findByUserIdAndActiveTrue(driverUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active driver profile not found for user id: "
                                        + driverUserId
                        )
                );

        return tripRepository
                .findAllByDriverIdAndActiveTrue(driver.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getDriverTripById(
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

        return mapToResponse(trip);
    }

    /** This prevents:
     * Driver A
     *    ↓
     * tries to access
     *    ↓
     * Driver B's trip
     *    ↓
     * BLOCKED
     * **/

    @Override
    public TripResponse startTripByDriver(
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

        if (trip.getStatus() != TripStatus.SCHEDULED
                && trip.getStatus() != TripStatus.BOARDING) {

            throw new BadRequestException(
                    "Trip can only be started from SCHEDULED or BOARDING status"
            );
        }

        trip.setStatus(TripStatus.IN_PROGRESS);
        trip.setActualStartTime(LocalDateTime.now());

        return mapToResponse(tripRepository.save(trip));
    }

//    complete trip
    @Override
    public TripResponse completeTripByDriver(
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

        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new BadRequestException(
                    "Only IN_PROGRESS trip can be completed"
            );
        }

        trip.setStatus(TripStatus.COMPLETED);
        trip.setActualEndTime(LocalDateTime.now());

        return mapToResponse(tripRepository.save(trip));
    }


    /**
     A driver can cancel:

     SCHEDULED
     ↓
     CANCELLED

     BOARDING
     ↓
     CANCELLED

     But not: IN_PROGRESS → CANCELLED

     That should be handled by Admin/operations.

     * **/
    @Override
    public TripResponse cancelTripByDriver(
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

        if (trip.getStatus() != TripStatus.SCHEDULED
                && trip.getStatus() != TripStatus.BOARDING) {

            throw new BadRequestException(
                    "Driver can only cancel SCHEDULED or BOARDING trips"
            );
        }

        trip.setStatus(TripStatus.CANCELLED);

        return mapToResponse(tripRepository.save(trip));
    }


    //   --------------------Driver logic end ---------------------//

    //-------------------- Passenger Logic Start --------------------//
    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getAvailableTrips() {

        List<TripStatus> statuses = List.of(
                TripStatus.SCHEDULED,
                TripStatus.BOARDING,
                TripStatus.IN_PROGRESS
        );

        return tripRepository
                .findAllByActiveTrueAndStatusInOrderByScheduledStartTimeAsc(
                        statuses
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getAvailableTripsByRoute(
            UUID routeId
    ) {

        routeRepository.findById(routeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Route not found with id: " + routeId
                        )
                );

        List<TripStatus> statuses = List.of(
                TripStatus.SCHEDULED,
                TripStatus.BOARDING,
                TripStatus.IN_PROGRESS
        );

        return tripRepository
                .findAllByRouteIdAndActiveTrueAndStatusInOrderByScheduledStartTimeAsc(
                        routeId,
                        statuses
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    //-------------------- Passenger Logic End --------------------//

    /**
     * Entity → Response DTO mapping.
     *
     * Henglish:
     * Database entity ko directly API response mein return nahi karenge.
     * DTO ke through controlled response denge.
     */
    private TripResponse mapToResponse(Trip trip) {

        Integer totalSeats = trip.getTotalSeats();

        Integer currentOccupancy =
                trip.getCurrentOccupancy() != null
                        ? trip.getCurrentOccupancy()
                        : 0;

        Integer availableSeats =
                totalSeats != null
                        ? totalSeats - currentOccupancy
                        : 0;

        return new TripResponse(
                trip.getId(),

                // Route information
                trip.getRoute().getId(),
                trip.getRoute().getRouteCode(),
                trip.getRoute().getRouteName(),

                // Bus information
                trip.getBus().getId(),
                trip.getBus().getBusNumber(),
                trip.getBus().getRegistrationNumber(),

                // Driver information
                trip.getDriver().getId(),
                trip.getDriver().getEmployeeCode(),
                trip.getDriver().getUser().getFullName(),

                // Schedule
                trip.getScheduledStartTime(),
                trip.getScheduledEndTime(),
                trip.getActualStartTime(),
                trip.getActualEndTime(),

                // Trip status
                trip.getStatus(),

                // Occupancy
                totalSeats,
                currentOccupancy,
                availableSeats,

                // Current GPS location
                trip.getCurrentLatitude(),
                trip.getCurrentLongitude(),
                trip.getLastLocationUpdate(),

                // Record metadata
                trip.getActive(),
                trip.getCreatedAt(),
                trip.getUpdatedAt()
        );
    }
}