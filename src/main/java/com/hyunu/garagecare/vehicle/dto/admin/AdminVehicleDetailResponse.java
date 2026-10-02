package com.hyunu.garagecare.vehicle.dto.admin;

import com.hyunu.garagecare.vehicle.domain.Vehicle;

public record AdminVehicleDetailResponse(
        Long vehicleId,
        String vehicleNumber,
        String manufacturer,
        String model,
        Integer modelYear,
        Long memberId,
        String memberName,
        String memberEmail
) {
    public static AdminVehicleDetailResponse from(Vehicle vehicle) {
        return new AdminVehicleDetailResponse(
                vehicle.getId(),
                vehicle.getVehicleNumber(),
                vehicle.getManufacturer(),
                vehicle.getModel(),
                vehicle.getModelYear(),
                vehicle.getMember().getId(),
                vehicle.getMember().getName(),
                vehicle.getMember().getEmail()
        );
    }
}
