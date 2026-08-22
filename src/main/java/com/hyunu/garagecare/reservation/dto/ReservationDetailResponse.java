package com.hyunu.garagecare.reservation.dto;

import com.hyunu.garagecare.reservation.domain.Reservation;
import com.hyunu.garagecare.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReservationDetailResponse(
        Long reservationId,
        Long vehicleId,
        String vehicleNumber,
        LocalDate reservationDate,
        LocalTime reservationTime,
        ReservationStatus status,
        List<ReservationItemResponse> maintenanceItems
) {
    public static ReservationDetailResponse from(Reservation reservation) {
        List<ReservationItemResponse> maintenanceItems =
                reservation.getReservationItems()
                        .stream()
                        .map(ReservationItemResponse::from)
                        .toList();

        return new ReservationDetailResponse(
                reservation.getId(),
                reservation.getVehicle().getId(),
                reservation.getVehicle().getVehicleNumber(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getStatus(),
                maintenanceItems
        );
    }
}
