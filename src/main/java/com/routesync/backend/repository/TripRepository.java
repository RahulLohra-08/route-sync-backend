package com.routesync.backend.repository;

import com.routesync.backend.entity.Trip;
import com.routesync.backend.entity.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {

    /*
     * Find all active trips.
     */
    List<Trip> findAllByActiveTrue();

    /*
     * Find active trips by route.
     */
    List<Trip> findAllByRouteIdAndActiveTrue(UUID routeId);

    /*
     * Find active trips by bus.
     */
    List<Trip> findAllByBusIdAndActiveTrue(UUID busId);

    /*
     * Find active trips by driver.
     */
    List<Trip> findAllByDriverIdAndActiveTrue(UUID driverId);

    /*
     * Find active trips by status.
     */
    List<Trip> findAllByStatusAndActiveTrue(TripStatus status);

    /*
     * Find a specific active trip.
     */
    Optional<Trip> findByIdAndActiveTrue(UUID tripId);

    /*
     * Find trips scheduled within a time range.
     */
    List<Trip> findAllByScheduledStartTimeBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    /*
     * Find active trips for a bus within a time range.
     *
     * Used to prevent overlapping bus assignments.
     */
    @Query("""
            SELECT t
            FROM Trip t
            WHERE t.bus.id = :busId
              AND t.active = true
              AND t.status <> com.routesync.backend.entity.enums.TripStatus.CANCELLED
              AND t.scheduledStartTime < :scheduledEnd
              AND t.scheduledEndTime > :scheduledStart
            """)
    List<Trip> findOverlappingTripsForBus(
            @Param("busId") UUID busId,
            @Param("scheduledStart") LocalDateTime scheduledStart,
            @Param("scheduledEnd") LocalDateTime scheduledEnd
    );

    /*
     * Find active trips for a driver within a time range.
     *
     * Used to prevent overlapping driver assignments.
     */
    @Query("""
            SELECT t
            FROM Trip t
            WHERE t.driver.id = :driverId
              AND t.active = true
              AND t.status <> com.routesync.backend.entity.enums.TripStatus.CANCELLED
              AND t.scheduledStartTime < :scheduledEnd
              AND t.scheduledEndTime > :scheduledStart
            """)
    List<Trip> findOverlappingTripsForDriver(
            @Param("driverId") UUID driverId,
            @Param("scheduledStart") LocalDateTime scheduledStart,
            @Param("scheduledEnd") LocalDateTime scheduledEnd
    );

    /*
     * Update-specific bus overlap check.
     *
     * Excludes the current trip from the search.
     */
    @Query("""
            SELECT t
            FROM Trip t
            WHERE t.bus.id = :busId
              AND t.id <> :tripId
              AND t.active = true
              AND t.status <> com.routesync.backend.entity.enums.TripStatus.CANCELLED
              AND t.scheduledStartTime < :scheduledEnd
              AND t.scheduledEndTime > :scheduledStart
            """)
    List<Trip> findOverlappingTripsForBusExcludingTrip(
            @Param("busId") UUID busId,
            @Param("tripId") UUID tripId,
            @Param("scheduledStart") LocalDateTime scheduledStart,
            @Param("scheduledEnd") LocalDateTime scheduledEnd
    );

    /*
     * Update-specific driver overlap check.
     *
     * Excludes the current trip from the search.
     */
    @Query("""
            SELECT t
            FROM Trip t
            WHERE t.driver.id = :driverId
              AND t.id <> :tripId
              AND t.active = true
              AND t.status <> com.routesync.backend.entity.enums.TripStatus.CANCELLED
              AND t.scheduledStartTime < :scheduledEnd
              AND t.scheduledEndTime > :scheduledStart
            """)
    List<Trip> findOverlappingTripsForDriverExcludingTrip(
            @Param("driverId") UUID driverId,
            @Param("tripId") UUID tripId,
            @Param("scheduledStart") LocalDateTime scheduledStart,
            @Param("scheduledEnd") LocalDateTime scheduledEnd
    );

    /*
     * Count active trips for a route.
     */
    long countByRouteIdAndActiveTrue(UUID routeId);

    /*
     * Count active trips for a bus.
     */
    long countByBusIdAndActiveTrue(UUID busId);

    /*
     * Count active trips for a driver.
     */
    long countByDriverIdAndActiveTrue(UUID driverId);

//    passenger related repository
    List<Trip> findAllByActiveTrueAndStatusInOrderByScheduledStartTimeAsc(
            List<TripStatus> statuses
    );

    List<Trip> findAllByRouteIdAndActiveTrueAndStatusInOrderByScheduledStartTimeAsc(
            UUID routeId,
            List<TripStatus> statuses
    );
}