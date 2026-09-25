package com.routesync.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "trip_locations",
        indexes = {
                @Index(name = "idx_trip_location_trip_id", columnList = "trip_id"),
                @Index(name = "idx_trip_location_timestamp", columnList = "recorded_at"),
                @Index(
                        name = "idx_trip_location_trip_timestamp",
                        columnList = "trip_id, recorded_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    // ==========================================
    // TRIP
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "trip_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_trip_location_trip")
    )
    private Trip trip;


    // ==========================================
    // GPS COORDINATES
    // ==========================================

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;


    // ==========================================
    // GPS INFORMATION
    // ==========================================

    /**
     * Speed of the bus in km/h.
     */
    private Double speed;

    /**
     * Direction of movement in degrees.
     *
     * 0   = North
     * 90  = East
     * 180 = South
     * 270 = West
     */
    private Double heading;

    /**
     * GPS accuracy in meters.
     */
    private Double accuracy;


    // ==========================================
    // TIMESTAMP
    // ==========================================

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;


    // ==========================================
    // CREATED TIMESTAMP
    // ==========================================

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    @PrePersist
    protected void onCreate() {

        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }

        createdAt = LocalDateTime.now();
    }
}