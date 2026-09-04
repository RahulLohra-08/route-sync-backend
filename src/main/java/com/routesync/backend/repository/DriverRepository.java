package com.routesync.backend.repository;

import com.routesync.backend.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    /**
     * Find driver by associated user ID.
     */
    Optional<Driver> findByUserId(UUID userId);

    /**
     * Find active driver by associated user ID.
     */
    Optional<Driver> findByUserIdAndActiveTrue(UUID userId);

    /**
     * Find driver using employee code.
     */
    Optional<Driver> findByEmployeeCode(String employeeCode);

    /**
     * Find driver using license number.
     */
    Optional<Driver> findByLicenseNumber(String licenseNumber);

    /**
     * Check whether a user already has a driver profile.
     */
    boolean existsByUserId(UUID userId);

    /**
     * Check whether employee code already exists.
     */
    boolean existsByEmployeeCode(String employeeCode);

    /**
     * Check whether license number already exists.
     */
    boolean existsByLicenseNumber(String licenseNumber);

    /**
     * Find all active drivers.
     */
    List<Driver> findAllByActiveTrue();

    /**
     * Find active drivers by current status.
     */
    List<Driver> findAllByStatusAndActiveTrue(
            com.routesync.backend.entity.enums.DriverStatus status
    );
}