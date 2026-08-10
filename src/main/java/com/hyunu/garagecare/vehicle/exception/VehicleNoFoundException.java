package com.hyunu.garagecare.vehicle.exception;

public class VehicleNoFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "차량을 찾을 수 없습니다.";

    public VehicleNoFoundException() {
        super(DEFAULT_MESSAGE);
    }
}
