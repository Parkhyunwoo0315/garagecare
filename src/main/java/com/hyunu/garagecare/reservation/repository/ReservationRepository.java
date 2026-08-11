package com.hyunu.garagecare.reservation.repository;

import com.hyunu.garagecare.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long>{

}
