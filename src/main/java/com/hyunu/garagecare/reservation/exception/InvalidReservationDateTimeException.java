package com.hyunu.garagecare.reservation.exception;

public class InvalidReservationDateTimeException extends RuntimeException {

    public InvalidReservationDateTimeException() {
        super("현재 시각 이전으로 예약할 수 없습니다.");
    }
}
