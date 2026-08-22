package com.hyunu.garagecare.reservation.repository;

import com.hyunu.garagecare.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long>{

    @EntityGraph(attributePaths = "vehicle")
    List<Reservation>
    findAllByMemberIdOrderByReservationDateDescReservationTimeDesc(
            Long memberId
    );

    @Query("""
            select distinct r
            from Reservation r
            join fetch r.member
            join fetch r.vehicle
            left join fetch r.reservationItems ri
            left join fetch ri.maintenanceItem
            where r.id = :reservationId
            """)
    Optional<Reservation> findDetailById(
            @Param("reservationId")
            Long reservationId
    );
}
