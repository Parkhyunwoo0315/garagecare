package com.hyunu.garagecare.reservation.exception;

public class EmptyMaintenanceItemException extends RuntimeException{

    private static final String DEFAUL_MESSAGE =
            "정비 항목을 하나 이상 선택해야 합니다.";

    public EmptyMaintenanceItemException() {
        super(DEFAUL_MESSAGE);
    }
}
