package com.routesync.backend.entity;

import com.routesync.backend.entity.enums.DriverStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.UUID;

@Entity
@Table(
        name = "drivers",
        indexes = {
                @Index(
                        name = "idx_driver_employee_code",
                        columnList = "employee_code"
                ),
                @Index(
                        name = "idx_driver_license_number",
                        columnList = "license_number"
                ),
                @Index(
                        name = "idx_driver_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_driver_active",
                        columnList = "active"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Existing user account associated with this driver.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    /**
     * Internal employee identifier.
     */
    @Column(
            name = "employee_code",
            nullable = false,
            unique = true,
            length = 30
    )
    private String employeeCode;

    /**
     * Driving license number.
     */
    @Column(
            name = "license_number",
            nullable = false,
            unique = true,
            length = 50
    )
    private String licenseNumber;

    /**
     * Driving license expiry date.
     */
    @Column(
            name = "license_expiry_date",
            nullable = false
    )
    private LocalDate licenseExpiryDate;

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
    private DriverStatus status = DriverStatus.AVAILABLE;

    /**
     * Soft-delete / deactivation flag.
     */
    @Column(
            name = "active",
            nullable = false
    )
    @Builder.Default
    private Boolean active = true;

    /**
     * Creation timestamp.
     */
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
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
            this.status = DriverStatus.AVAILABLE;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = LocalDateTime.now();
    }
}
