package com.routesync.backend.entity;

import com.routesync.backend.entity.enums.BusStatus;
import com.routesync.backend.entity.enums.BusType;
import com.routesync.backend.entity.enums.FuelType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "buses",
        indexes = {
                @Index(
                        name = "idx_bus_registration_number",
                        columnList = "registration_number"
                ),
                @Index(
                        name = "idx_bus_bus_number",
                        columnList = "bus_number"
                ),
                @Index(
                        name = "idx_bus_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_bus_active",
                        columnList = "active"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Official vehicle registration number.
     * Example: JH05AB1234
     */
    @Column(
            name = "registration_number",
            nullable = false,
            unique = true,
            length = 20
    )
    private String registrationNumber;

    /**
     * Operational/display number of the bus.
     * Example: RS-101
     */
    @Column(
            name = "bus_number",
            nullable = false,
            unique = true,
            length = 20
    )
    private String busNumber;

    /**
     * Bus model.
     */
    @Column(
            name = "model",
            length = 100
    )
    private String model;

    /**
     * Vehicle manufacturer.
     */
    @Column(
            name = "manufacturer",
            length = 100
    )
    private String manufacturer;

    /**
     * Maximum passenger capacity.
     */
    @Column(
            name = "capacity",
            nullable = false
    )
    private Integer capacity;

    /**
     * Operational type of bus.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "bus_type",
            nullable = false,
            length = 20
    )
    private BusType busType;

    /**
     * Fuel / energy source.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "fuel_type",
            nullable = false,
            length = 20
    )
    private FuelType fuelType;

    /**
     * Current operational status.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private BusStatus status;

    /**
     * Year in which the vehicle was manufactured.
     */
    @Column(name = "year_of_manufacture")
    private Integer yearOfManufacture;

    /**
     * Latest known GPS latitude.
     */
    @Column(name = "current_latitude")
    private Double currentLatitude;

    /**
     * Latest known GPS longitude.
     */
    @Column(name = "current_longitude")
    private Double currentLongitude;

    /**
     * Timestamp of the latest GPS location update.
     */
    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    /**
     * Soft-delete / operational activation flag.
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

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.active == null) {
            this.active = true;
        }

        if (this.status == null) {
            this.status = BusStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = LocalDateTime.now();
    }
}