package com.hyunu.garagecare.maintenance.dto.admin;

import com.hyunu.garagecare.maintenance.domain.MaintenanceItem;

public record AdminMaintenanceItemListResponse(
        Long maintenanceItemId,
        String name,
        String description,
        Long estimatedPrice,
        boolean active
) {
    public static AdminMaintenanceItemListResponse from(
            MaintenanceItem maintenanceItem
    ) {
        return new AdminMaintenanceItemListResponse(
                maintenanceItem.getId(),
                maintenanceItem.getName(),
                maintenanceItem.getDescription(),
                maintenanceItem.getEstimatedPrice(),
                maintenanceItem.isActive()
        );
    }
}
