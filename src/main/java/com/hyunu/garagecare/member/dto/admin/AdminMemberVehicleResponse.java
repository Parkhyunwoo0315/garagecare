package com.hyunu.garagecare.member.dto.admin;

import com.hyunu.garagecare.vehicle.domain.Vehicle;

public record AdminMemberVehicleResponse(
        Long vehicleId,
        String vehicleNumber,
        String manufacturer,
        String model,
        Integer modelYear
) {

    public static AdminMemberVehicleResponse from(Vehicle vehicle) {
        return new AdminMemberVehicleResponse(
                vehicle.getId(),
                vehicle.getVehicleNumber(),
                vehicle.getManufacturer(),
                vehicle.getModel(),
                vehicle.getModelYear()
        );
    }
}
