package com.hyunu.garagecare.vehicle.dto;

import com.hyunu.garagecare.vehicle.domain.Vehicle;

public record VehicleResponse(
        Long id,
        String vehicleNumber,
        String manufacturer,
        String model,
        Integer modelYear
) {

    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVehicleNumber(),
                vehicle.getManufacturer(),
                vehicle.getModel(),
                vehicle.getModelYear()
        );
    }
}
