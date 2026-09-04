package com.routesync.backend.service;

import com.routesync.backend.dto.driver.CreateDriverRequest;
import com.routesync.backend.dto.driver.DriverResponse;
import com.routesync.backend.dto.driver.UpdateDriverRequest;

import java.util.List;
import java.util.UUID;

public interface DriverService {

    DriverResponse createDriver(CreateDriverRequest request);

    DriverResponse getDriverById(UUID id);

    DriverResponse getDriverByUserId(UUID userId);

    List<DriverResponse> getAllActiveDrivers();

    DriverResponse updateDriver(UUID id, UpdateDriverRequest request);

    void deactivateDriver(UUID id);
}