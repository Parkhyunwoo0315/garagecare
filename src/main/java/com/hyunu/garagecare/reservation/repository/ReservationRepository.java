package com.hyunu.garagecare.reservation.repository;

import com.hyunu.garagecare.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long>{

    @EntityGraph(attributePaths = "vehicle")
    List<Reservation>
    findAllByMemberIdOrderByReservationDateDescReservationTimeDesc(
            Long memberId
    );
}
