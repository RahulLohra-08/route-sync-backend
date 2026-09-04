package com.routesync.backend.service.impl;

import com.routesync.backend.dto.bus.BusResponse;
import com.routesync.backend.dto.bus.CreateBusRequest;
import com.routesync.backend.dto.bus.UpdateBusRequest;
import com.routesync.backend.entity.Bus;
import com.routesync.backend.exception.DuplicateResourceException;
import com.routesync.backend.exception.ResourceNotFoundException;
import com.routesync.backend.repository.BusRepository;
import com.routesync.backend.service.BusService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BusServiceImpl implements BusService {

    private final BusRepository busRepository;

    @Override
    public BusResponse createBus(CreateBusRequest request) {

        // Check duplicate registration number
        if (busRepository.existsByRegistrationNumber(
                request.registrationNumber())) {

            throw new DuplicateResourceException(
                    "Bus with registration number "
                            + request.registrationNumber()
                            + " already exists"
            );
        }

        // Check duplicate operational bus number
        if (busRepository.existsByBusNumber(
                request.busNumber())) {

            throw new DuplicateResourceException(
                    "Bus with bus number "
                            + request.busNumber()
                            + " already exists"
            );
        }

        Bus bus = Bus.builder()
                .registrationNumber(request.registrationNumber())
                .busNumber(request.busNumber())
                .model(request.model())
                .manufacturer(request.manufacturer())
                .capacity(request.capacity())
                .busType(request.busType())
                .fuelType(request.fuelType())
                .yearOfManufacture(request.yearOfManufacture())
                .build();

        Bus savedBus = busRepository.save(bus);

        return mapToResponse(savedBus);
    }

    @Override
    @Transactional(readOnly = true)
    public BusResponse getBusById(UUID id) {

        Bus bus = busRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bus not found with id: " + id
                        )
                );

        return mapToResponse(bus);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusResponse> getAllActiveBuses() {

        return busRepository.findAllByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public BusResponse updateBus(
            UUID id,
            UpdateBusRequest request
    ) {

        Bus bus = busRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bus not found with id: " + id
                        )
                );

        /*
         * Check registration number only when
         * it is actually changed.
         */
        if (request.registrationNumber() != null
                && !request.registrationNumber()
                .equals(bus.getRegistrationNumber())) {

            if (busRepository.existsByRegistrationNumber(
                    request.registrationNumber())) {

                throw new DuplicateResourceException(
                        "Bus with registration number "
                                + request.registrationNumber()
                                + " already exists"
                );
            }

            bus.setRegistrationNumber(
                    request.registrationNumber()
            );
        }

        /*
         * Check operational bus number only when
         * it is actually changed.
         */
        if (request.busNumber() != null
                && !request.busNumber()
                .equals(bus.getBusNumber())) {

            if (busRepository.existsByBusNumber(
                    request.busNumber())) {

                throw new DuplicateResourceException(
                        "Bus with bus number "
                                + request.busNumber()
                                + " already exists"
                );
            }

            bus.setBusNumber(request.busNumber());
        }

        if (request.model() != null) {
            bus.setModel(request.model());
        }

        if (request.manufacturer() != null) {
            bus.setManufacturer(request.manufacturer());
        }

        if (request.capacity() != null) {
            bus.setCapacity(request.capacity());
        }

        if (request.busType() != null) {
            bus.setBusType(request.busType());
        }

        if (request.fuelType() != null) {
            bus.setFuelType(request.fuelType());
        }

        if (request.status() != null) {
            bus.setStatus(request.status());
        }

        if (request.yearOfManufacture() != null) {
            bus.setYearOfManufacture(
                    request.yearOfManufacture()
            );
        }

        Bus updatedBus = busRepository.save(bus);

        return mapToResponse(updatedBus);
    }

    @Override
    public void deactivateBus(UUID id) {

        Bus bus = busRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bus not found with id: " + id
                        )
                );

        bus.setActive(false);

        busRepository.save(bus);
    }

    /**
     * Convert Bus entity to API response DTO.
     */
    private BusResponse mapToResponse(Bus bus) {

        return new BusResponse(
                bus.getId(),
                bus.getRegistrationNumber(),
                bus.getBusNumber(),
                bus.getModel(),
                bus.getManufacturer(),
                bus.getCapacity(),
                bus.getBusType(),
                bus.getFuelType(),
                bus.getStatus(),
                bus.getYearOfManufacture(),
                bus.getCurrentLatitude(),
                bus.getCurrentLongitude(),
                bus.getLastLocationUpdate(),
                bus.getActive(),
                bus.getCreatedAt(),
                bus.getUpdatedAt()
        );
    }
}