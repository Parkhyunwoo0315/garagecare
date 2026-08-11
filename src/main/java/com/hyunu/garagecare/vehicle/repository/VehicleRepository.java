package com.hyunu.garagecare.vehicle.repository;

import com.hyunu.garagecare.vehicle.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository
        extends JpaRepository<Vehicle, Long> {

    boolean existsByVehicleNumber(String vehicleNumber);
}