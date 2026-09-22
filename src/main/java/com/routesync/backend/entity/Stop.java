package com.routesync.backend.entity;

import com.routesync.backend.entity.enums.StopStatus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "stops",
        indexes = {
                @Index(name = "idx_stops_route_id", columnList = "route_id"),
                @Index(name = "idx_stops_stop_order", columnList = "stop_order"),
                @Index(name = "idx_stops_active", columnList = "active")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Route to which this stop belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "route_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_stops_route"
            )
    )
    private Route route;

    @Column(name = "stop_name", nullable = false, length = 150)
    private String stopName;

    @Column(length = 255)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    @Column(name = "estimated_arrival_offset_minutes")
    private Integer estimatedArrivalOffsetMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StopStatus status = StopStatus.ACTIVE;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = StopStatus.ACTIVE;
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