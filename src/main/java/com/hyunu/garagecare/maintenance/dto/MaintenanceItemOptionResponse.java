package com.hyunu.garagecare.maintenance.dto;

import com.hyunu.garagecare.maintenance.domain.MaintenanceItem;

public record MaintenanceItemOptionResponse(
        Long id,
        String name,
        Long estimatedPrice
) {
    public static MaintenanceItemOptionResponse from(MaintenanceItem maintenanceItem) {
        return new MaintenanceItemOptionResponse(
                maintenanceItem.getId(),
                maintenanceItem.getName(),
                maintenanceItem.getEstimatedPrice()
        );
    }
}
