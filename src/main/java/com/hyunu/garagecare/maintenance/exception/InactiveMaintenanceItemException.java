package com.hyunu.garagecare.maintenance.exception;

public class InactiveMaintenanceItemException extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "현재 선택할 수 없는 정비 항목이 포함되어 있습니다.";

    public InactiveMaintenanceItemException() {
        super(DEFAULT_MESSAGE);
    }
}
