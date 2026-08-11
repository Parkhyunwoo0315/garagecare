package com.hyunu.garagecare.reservation.exception;

public class DuplicateMaintenanceItemException extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "동일한 정비 항목을 중복해서 선택할 수 없습니다.";

    public DuplicateMaintenanceItemException() {
        super(DEFAULT_MESSAGE);
    }
}
