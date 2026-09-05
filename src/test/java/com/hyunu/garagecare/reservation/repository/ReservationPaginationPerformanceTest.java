package com.hyunu.garagecare.reservation.repository;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.reservation.domain.Reservation;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
public class ReservationPaginationPerformanceTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Test
    @DisplayName("PostgreSQL Offset Pagination 실행계획 비교")
    void compareOffsetPaginationPerformance() {

        //given
        Member member = createTestData();

        entityManager.flush();
        entityManager.clear();

        analyzeReservations();

        System.out.println();
        System.out.println("===== OFFSET =====");
        System.out.println(getPaginationPlan(member.getId(), 0));

        System.out.println();
        System.out.println("===== OFFSET 1,000 =====");
        System.out.println(getPaginationPlan(member.getId(), 1_000));

        System.out.println();
        System.out.println("===== OFFSET 5,000 =====");
        System.out.println(getPaginationPlan(member.getId(), 5_000));

        System.out.println();
        System.out.println("===== OFFSET 9,000 =====");
        System.out.println(getPaginationPlan(member.getId(), 9_000));

        System.out.println();
        System.out.println("===== COUNT QUERY =====");
        System.out.println(getCountPlan(member.getId()));
    }

    private List<?> getPaginationPlan(
            Long memberId,
            int offset
    ) {
        return entityManager.createNativeQuery("""
                EXPLAIN (ANALYZE, BUFFERS)
                SELECT *
                FROM reservations
                WHERE member_id = :memberId
                ORDER BY reservation_date DESC,
                         reservation_time DESC 
                LIMIT 10
                OFFSET :offset
                """)
                .setParameter("memberId", memberId)
                .setParameter("offset", offset)
                .getResultList();
    }

    private List<?> getCountPlan(
            Long memberId
    ) {
        return entityManager.createNativeQuery("""
                EXPLAIN (ANALYZE, BUFFERS)
                SELECT COUNT(*)
                FROM reservations
                WHERE member_id = :memberId
                """)
                .setParameter("memberId", memberId)
                .getResultList();
    }

    private void analyzeReservations() {
        entityManager.createNativeQuery("""
                ANALYZE reservations
                """)
                .executeUpdate();
    }

    private Member createTestData() {

        Member member = createMember();

        Vehicle vehicle = createVehicle(member);

        LocalDate baseDate =
                LocalDate.of(2020, 1, 1);

        for (int i = 0; i < 10_000; i++) {
            Reservation reservation =
                    Reservation.create(
                            member,
                            vehicle,
                            baseDate.plusDays(i % 2_000),
                            LocalTime.of(
                                    9 + (i % 9),
                                    i % 60
                            )
                    );

            reservationRepository.save(reservation);

            if (i > 0 && i % 500 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.flush();
        entityManager.clear();

        return member;
    }

    private Member createMember() {

        return memberRepository.save(
                Member.create(
                        "Pagination Performance Member",
                        "pagination-performance@test.com",
                        "encoded-password"
                )
        );
    }

    private Vehicle createVehicle(
            Member member
    ) {
        return vehicleRepository.save(
                Vehicle.create(
                        member,
                        "67가8910",
                        "Nissan",
                        "Skyline GT-R R34",
                        2001
                )
        );
    }
}
