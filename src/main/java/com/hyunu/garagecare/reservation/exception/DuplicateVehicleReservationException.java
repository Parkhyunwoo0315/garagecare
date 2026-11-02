package com.hyunu.garagecare.reservation.exception;

public class DuplicateVehicleReservationException extends RuntimeException {
    public DuplicateVehicleReservationException() {
        super("해당 차량은 선택한 날짜와 시간에 미이 예약되어 있습니다.");
    }
}
