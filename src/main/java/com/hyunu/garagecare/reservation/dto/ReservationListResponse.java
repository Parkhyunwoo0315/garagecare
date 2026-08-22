package com.hyunu.garagecare.reservation.dto;

import com.hyunu.garagecare.reservation.domain.Reservation;
import com.hyunu.garagecare.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationListResponse(
        Long reservationId,
        String vehicleNumber,
        LocalDate reservationDate,
        LocalTime reservationTime,
        ReservationStatus status
) {
    public static ReservationListResponse from(Reservation reservation) {
        return new ReservationListResponse(
                reservation.getId(),
                reservation.getVehicle().getVehicleNumber(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getStatus()
        );
    }
}
