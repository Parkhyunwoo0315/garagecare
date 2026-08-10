package com.hyunu.garagecare.reservation.exception;

public class UnauthorizedVehicleAccessException extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "해당 차량에 대한 접근 권한이 없습니다.";

    public UnauthorizedVehicleAccessException() {
        super(DEFAULT_MESSAGE);
    }
}
