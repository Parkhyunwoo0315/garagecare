package com.hyunu.garagecare.maintenance.exception;

public class MaintenanceItemNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "정비 항목을 찾을 수 없습니다.";

    public MaintenanceItemNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
}
