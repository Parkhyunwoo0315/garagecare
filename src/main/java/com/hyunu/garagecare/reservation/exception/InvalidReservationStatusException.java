package com.hyunu.garagecare.reservation.exception;

public class InvalidReservationStatusException extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "현재 상태에서는 예약을 변경할 수 없습니다.";

    public InvalidReservationStatusException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidReservationStatusException(String message) {
        super(message);
    }
}
