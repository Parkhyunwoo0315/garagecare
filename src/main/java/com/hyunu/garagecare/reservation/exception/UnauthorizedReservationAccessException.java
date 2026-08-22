package com.hyunu.garagecare.reservation.exception;

public class UnauthorizedReservationAccessException extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "해당 예약에 대한 접근 권한이 없습니다.";

    public UnauthorizedReservationAccessException() {
        super(DEFAULT_MESSAGE);
    }
}
