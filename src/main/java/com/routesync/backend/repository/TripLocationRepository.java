package com.routesync.backend.repository;

import com.routesync.backend.entity.TripLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripLocationRepository
        extends JpaRepository<TripLocation, UUID> {

    List<TripLocation> findAllByTripIdOrderByRecordedAtAsc(
            UUID tripId
    );

    List<TripLocation> findAllByTripIdOrderByRecordedAtDesc(
            UUID tripId
    );

    List<TripLocation> findAllByTripIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            UUID tripId,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<TripLocation> findFirstByTripIdOrderByRecordedAtDesc(
            UUID tripId
    );
}