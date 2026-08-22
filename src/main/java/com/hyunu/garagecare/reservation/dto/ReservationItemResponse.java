package com.hyunu.garagecare.reservation.dto;

import com.hyunu.garagecare.reservation.domain.ReservationItem;

public record ReservationItemResponse (
    Long maintenanceItemId,
    String name,
    Long estimatedPrice
) {
    public static ReservationItemResponse from(ReservationItem reservationItem) {
        return new ReservationItemResponse(
                reservationItem.getMaintenanceItem().getId(),
                reservationItem.getMaintenanceItem().getName(),
                reservationItem.getMaintenanceItem().getEstimatedPrice()
        );
    }
}
