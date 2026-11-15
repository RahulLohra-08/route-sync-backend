package com.routesync.backend.entity;

import com.routesync.backend.entity.enums.RouteStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Route entity represents a predefined public transport route.
 *
 * Example:
 *
 * Route Name:
 * Jamshedpur to Ranchi
 *
 * Route Code:
 * RS-JSR-RNC-001
 *
 * A route will later contain multiple ordered stops.
 *
 * Route -> Route Stops -> Trip -> Bus + Driver
 */
@Entity
@Table(
        name = "routes",
        indexes = {

                @Index(
                        name = "idx_route_code",
                        columnList = "route_code"
                ),

                @Index(
                        name = "idx_route_status",
                        columnList = "status"
                ),

                @Index(
                        name = "idx_route_active",
                        columnList = "active"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    /**
     * Unique route identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    /**
     * Unique business identifier for the route.
     *
     * Example:
     * RS-JSR-RNC-001
     */
    @Column(
            name = "route_code",
            nullable = false,
            unique = true,
            length = 30
    )
    private String routeCode;


    /**
     * Display name of the route.
     *
     * Example:
     * Jamshedpur to Ranchi
     */
    @Column(
            name = "route_name",
            nullable = false,
            length = 150
    )
    private String routeName;


    /**
     * Starting location of the route.
     *
     * Example:
     * Jamshedpur
     */
    @Column(
            name = "start_location",
            nullable = false,
            length = 150
    )
    private String startLocation;


    /**
     * Ending location of the route.
     *
     * Example:
     * Ranchi
     */
    @Column(
            name = "end_location",
            nullable = false,
            length = 150
    )
    private String endLocation;


    /**
     * Estimated total distance of the route in kilometres.
     *
     * This is an operational estimate.
     * Later, it can be calculated using map services.
     */
    @Column(name = "distance_km")
    private Double distanceKm;


    /**
     * Estimated duration of the complete route in minutes.
     */
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;


    /**
     * Current operational status.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private RouteStatus status = RouteStatus.ACTIVE;


    /**
     * Soft-delete / activation flag.
     */
    @Column(
            name = "active",
            nullable = false
    )
    @Builder.Default
    private Boolean active = true;


    /**
     * Record creation timestamp.
     */
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;


    /**
     * Record update timestamp.
     */
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;


    /**
     * Automatically set timestamps and default values
     * before inserting a new route.
     */
    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.status == null) {
            this.status = RouteStatus.ACTIVE;
        }

        if (this.active == null) {
            this.active = true;
        }
    }


    /**
     * Automatically update the modification timestamp.
     */
    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = LocalDateTime.now();
    }
}