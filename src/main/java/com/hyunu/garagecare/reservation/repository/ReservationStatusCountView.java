package com.hyunu.garagecare.reservation.repository;

import com.hyunu.garagecare.reservation.domain.ReservationStatus;

public interface ReservationStatusCountView {

    ReservationStatus getStatus();

    Long getTotalCount();
}