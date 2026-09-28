package com.hyunu.garagecare.reservation.dto.admin;

import com.hyunu.garagecare.reservation.domain.Reservation;
import com.hyunu.garagecare.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record AdminReservationListResponse(
        Long reservationId,
        Long memberId,
        String memberName,
        String memberEmail,
        Long vehicleId,
        String vehicleNumber,
        String vehicleModel,
        LocalDate reservationDate,
        LocalTime reservationTime,
        ReservationStatus status
) {
    public static AdminReservationListResponse from(
            Reservation reservation
    ) {
        return new AdminReservationListResponse(
                reservation.getId(),
                reservation.getMember().getId(),
                reservation.getMember().getName(),
                reservation.getMember().getEmail(),
                reservation.getVehicle().getId(),
                reservation.getVehicle().getVehicleNumber(),
                reservation.getVehicle().getModel(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getStatus()
        );
    }
}
