package com.hyunu.garagecare.reservation.exception;

public class ReservationNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "예약을 찾을 수 없습니다.";

    public ReservationNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
}
