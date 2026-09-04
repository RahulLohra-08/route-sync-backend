package com.routesync.backend.service.impl;

import com.routesync.backend.dto.driver.CreateDriverRequest;
import com.routesync.backend.dto.driver.DriverResponse;
import com.routesync.backend.dto.driver.UpdateDriverRequest;
import com.routesync.backend.entity.Driver;
import com.routesync.backend.entity.User;
import com.routesync.backend.entity.enums.DriverStatus;
import com.routesync.backend.exception.ResourceAlreadyExistsException;
import com.routesync.backend.exception.ResourceNotFoundException;
import com.routesync.backend.repository.DriverRepository;
import com.routesync.backend.repository.UserRepository;
import com.routesync.backend.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;

    /**
     * Create a new driver profile for an existing user.
     */
    @Override
    public DriverResponse createDriver(CreateDriverRequest request) {

        // Check whether user exists
        User user = userRepository.findById(request.userId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + request.userId()
                        )
                );

        // Check whether user already has a driver profile
        if (driverRepository.existsByUserId(request.userId())) {
            throw new ResourceAlreadyExistsException(
                    "Driver profile already exists for user id: "
                            + request.userId()
            );
        }

        // Check duplicate employee code
        if (driverRepository.existsByEmployeeCode(
                request.employeeCode()
        )) {
            throw new ResourceAlreadyExistsException(
                    "Employee code already exists: "
                            + request.employeeCode()
            );
        }

        // Check duplicate license number
        if (driverRepository.existsByLicenseNumber(
                request.licenseNumber()
        )) {
            throw new ResourceAlreadyExistsException(
                    "License number already exists: "
                            + request.licenseNumber()
            );
        }

        // Build Driver entity
        Driver driver = Driver.builder()
                .user(user)
                .employeeCode(request.employeeCode())
                .licenseNumber(request.licenseNumber())
                .licenseExpiryDate(request.licenseExpiryDate())
                .status(DriverStatus.AVAILABLE)
                .active(true)
                .build();

        // Save driver
        Driver savedDriver = driverRepository.save(driver);

        return mapToResponse(savedDriver);
    }

    /**
     * Get active driver by driver ID.
     */
    @Override
    @Transactional(readOnly = true)
    public DriverResponse getDriverById(UUID id) {

        Driver driver = driverRepository.findById(id)
                .filter(Driver::getActive)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active driver not found with id: " + id
                        )
                );

        return mapToResponse(driver);
    }

    /**
     * Get active driver by associated user ID.
     */
    @Override
    @Transactional(readOnly = true)
    public DriverResponse getDriverByUserId(UUID userId) {

        Driver driver = driverRepository
                .findByUserIdAndActiveTrue(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active driver not found for user id: "
                                        + userId
                        )
                );

        return mapToResponse(driver);
    }

    /**
     * Get all active drivers.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> getAllActiveDrivers() {

        return driverRepository.findAllByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Update an existing active driver.
     */
    @Override
    public DriverResponse updateDriver(
            UUID id,
            UpdateDriverRequest request
    ) {

        Driver driver = driverRepository.findById(id)
                .filter(Driver::getActive)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active driver not found with id: " + id
                        )
                );

        /*
         * Update employee code
         */
        if (request.employeeCode() != null
                && !request.employeeCode()
                .equals(driver.getEmployeeCode())) {

            if (driverRepository.existsByEmployeeCode(
                    request.employeeCode()
            )) {
                throw new ResourceAlreadyExistsException(
                        "Employee code already exists: "
                                + request.employeeCode()
                );
            }

            driver.setEmployeeCode(request.employeeCode());
        }

        /*
         * Update license number
         */
        if (request.licenseNumber() != null
                && !request.licenseNumber()
                .equals(driver.getLicenseNumber())) {

            if (driverRepository.existsByLicenseNumber(
                    request.licenseNumber()
            )) {
                throw new ResourceAlreadyExistsException(
                        "License number already exists: "
                                + request.licenseNumber()
                );
            }

            driver.setLicenseNumber(request.licenseNumber());
        }

        /*
         * Update license expiry date
         */
        if (request.licenseExpiryDate() != null) {
            driver.setLicenseExpiryDate(
                    request.licenseExpiryDate()
            );
        }

        /*
         * Update operational status
         */
        if (request.status() != null) {
            driver.setStatus(request.status());
        }

        Driver updatedDriver = driverRepository.save(driver);

        return mapToResponse(updatedDriver);
    }

    /**
     * Deactivate driver using soft-delete approach.
     */
    @Override
    public void deactivateDriver(UUID id) {

        Driver driver = driverRepository.findById(id)
                .filter(Driver::getActive)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active driver not found with id: " + id
                        )
                );

        /*
         * Soft deactivation.
         *
         * We do NOT physically delete the driver because
         * historical trip, tracking and analytics data
         * may depend on this driver.
         */
        driver.setActive(false);

        /*
         * Since the driver is no longer active,
         * mark them OFF_DUTY rather than inventing
         * an INACTIVE enum value.
         */
        driver.setStatus(DriverStatus.OFF_DUTY);

        driverRepository.save(driver);
    }

    /**
     * Convert Driver entity into DriverResponse.
     */
    private DriverResponse mapToResponse(Driver driver) {

        return new DriverResponse(
                driver.getId(),
                driver.getUser().getId(),
                driver.getEmployeeCode(),
                driver.getLicenseNumber(),
                driver.getLicenseExpiryDate(),
                driver.getStatus(),
                driver.getActive(),
                driver.getCreatedAt(),
                driver.getUpdatedAt()
        );
    }
}