package com.routesync.backend.entity;

import com.routesync.backend.entity.enums.TripStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "trips",
        indexes = {
                @Index(
                        name = "idx_trips_route_id",
                        columnList = "route_id"
                ),
                @Index(
                        name = "idx_trips_bus_id",
                        columnList = "bus_id"
                ),
                @Index(
                        name = "idx_trips_driver_id",
                        columnList = "driver_id"
                ),
                @Index(
                        name = "idx_trips_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_trips_scheduled_start",
                        columnList = "scheduled_start_time"
                ),
                @Index(
                        name = "idx_trips_active",
                        columnList = "active"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Route on which this trip operates.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "route_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_trips_route")
    )
    private Route route;

    /**
     * Bus assigned to this trip.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bus_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_trips_bus")
    )
    private Bus bus;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "current_occupancy", nullable = false)
    private Integer currentOccupancy = 0;

    /**
     * Driver assigned to this trip.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "driver_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_trips_driver")
    )
    private Driver driver;

    /**
     * Planned departure time.
     */
    @Column(name = "scheduled_start_time", nullable = false)
    private LocalDateTime scheduledStartTime;

    /**
     * Planned arrival time.
     */
    @Column(name = "scheduled_end_time", nullable = false)
    private LocalDateTime scheduledEndTime;

    /**
     * Actual time when the bus started the trip.
     */
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * Actual time when the bus completed the trip.
     */
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    /**
     * Current lifecycle state of the trip.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripStatus status = TripStatus.SCHEDULED;

    /**
     * Latest GPS latitude.
     */
    @Column(name = "current_latitude")
    private Double currentLatitude;

    /**
     * Latest GPS longitude.
     */
    @Column(name = "current_longitude")
    private Double currentLongitude;

    /**
     * Timestamp of the latest GPS update.
     */
    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    /**
     * Soft-delete / active flag.
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Record creation timestamp.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Record update timestamp.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = TripStatus.SCHEDULED;
        }

        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}