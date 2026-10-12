package com.hyunu.garagecare.admin.dashboard.dto;

public record AdminDashboardResponse(
        Long memberCount,
        Long vehicleCount,
        Long reservationCount,
        Long pendingReservationCount,
        Long confirmedReservationCount,
        Long completedReservationCount,
        Long canceledReservationCount,
        Long maintenanceItemCount,
        Long activeMaintenanceItemCount,
        Long inactiveMaintenanceItemCount
) {
}
