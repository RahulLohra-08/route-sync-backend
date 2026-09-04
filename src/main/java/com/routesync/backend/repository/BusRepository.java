package com.routesync.backend.repository;

import com.routesync.backend.entity.Bus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusRepository extends JpaRepository<Bus, UUID> {

    /**
     * Find bus using official vehicle registration number.
     */
    Optional<Bus> findByRegistrationNumber(String registrationNumber);

    /**
     * Find bus using operational/display bus number.
     */
    Optional<Bus> findByBusNumber(String busNumber);

    /**
     * Check whether registration number already exists.
     */
    boolean existsByRegistrationNumber(String registrationNumber);

    /**
     * Check whether operational bus number already exists.
     */
    boolean existsByBusNumber(String busNumber);

    /**
     * Find only active buses.
     */
    Optional<Bus> findByIdAndActiveTrue(UUID id);

    /**
     * Check whether an active bus exists.
     */
    boolean existsByIdAndActiveTrue(UUID id);

    List<Bus> findAllByActiveTrue();

}