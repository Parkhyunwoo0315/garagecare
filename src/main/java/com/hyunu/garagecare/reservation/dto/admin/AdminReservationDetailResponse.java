package com.hyunu.garagecare.reservation.dto.admin;

import com.hyunu.garagecare.reservation.domain.Reservation;
import com.hyunu.garagecare.reservation.domain.ReservationStatus;
import com.hyunu.garagecare.reservation.dto.ReservationItemResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AdminReservationDetailResponse(
        Long reservationId,
        String memberName,
        String memberEmail,
        String vehicleNumber,
        String manufacturer,
        String model,
        Integer modelYear,
        LocalDate reservationDate,
        LocalTime reservationTime,
        ReservationStatus status,
        List<ReservationItemResponse> maintenanceItems
) {

    public static AdminReservationDetailResponse from(
            Reservation reservation
    ) {
        return new AdminReservationDetailResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getMember().getEmail(),
                reservation.getVehicle().getVehicleNumber(),
                reservation.getVehicle().getManufacturer(),
                reservation.getVehicle().getModel(),
                reservation.getVehicle().getModelYear(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getStatus(),
                reservation.getReservationItems()
                        .stream()
                        .map(ReservationItemResponse::from)
                        .toList()
        );
    }
}
