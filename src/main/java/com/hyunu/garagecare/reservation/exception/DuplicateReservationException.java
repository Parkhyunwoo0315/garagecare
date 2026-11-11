
package com.hyunu.garagecare.reservation.exception;

public class DuplicateReservationException
        extends RuntimeException {

    public DuplicateReservationException() {
        super(
                "해당 차량은 선택하신 날짜와 시간에 "
                        + "이미 예약되어 있습니다. "
                        + "기존 예약을 확인하거나 "
                        + "다른 시간을 선택해 주세요."
        );
    }
}
