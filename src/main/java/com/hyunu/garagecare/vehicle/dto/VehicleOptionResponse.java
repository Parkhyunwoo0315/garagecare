package com.hyunu.garagecare.vehicle.dto;

import com.hyunu.garagecare.vehicle.domain.Vehicle;

public record VehicleOptionResponse (
    Long id,
    String displayName
) {
    public static VehicleOptionResponse from(Vehicle vehicle) {
        return new VehicleOptionResponse(
                vehicle.getId(),
                vehicle.getVehicleNumber()
                        + "(" + vehicle.getVehicleNumber() + ")"
        );
    }
}
