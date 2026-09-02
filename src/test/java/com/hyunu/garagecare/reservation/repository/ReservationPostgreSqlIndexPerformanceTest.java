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
public class ReservationPostgreSqlIndexPerformanceTest {

    private static final String INDEX_NAME =
            "idx_reservation_member_date_time";

    @Autowired
    EntityManager entityManager;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Test
    @DisplayName("PostgreSQL 예약 조회 복합 인덱스 적용 전후 실행계획 비교")
    void compareReservationIndexPerformance() {

        //given
        Member targetMember = createTestData();

        entityManager.flush();
        entityManager.clear();

        // =========================
        // BEFORE
        // =========================

        dropReservationIndex();

        analyzeReservations();

        List<?> beforePlan =
                getExecutionPlan(targetMember.getId());

        System.out.println();
        System.out.println("===== BEFORE INDEX =====");
        printExecutionPlan(beforePlan);

        // =========================
        // AFTER
        // =========================

        createReservationIndex();

        analyzeReservations();

        List<?> afterPlan =
                getExecutionPlan(targetMember.getId());

        System.out.println();
        System.out.println("===== AFTER INDEX =====");
        printExecutionPlan(afterPlan);
    }

    private List<?> getExecutionPlan(Long memberId) {

        return entityManager.createNativeQuery("""
                 EXPLAIN (ANALYZE, BUFFERS)
                 SELECT *
                 FROM reservations
                 WHERE member_id = :memberId
                 ORDER BY reservation_date DESC,
                          reservation_time DESC
                 """)
                .setParameter("memberId",memberId)
                .getResultList();
    }

    private void dropReservationIndex() {

        entityManager.createNativeQuery("""
                DROP INDEX IF EXISTS idx_reservation_member_date_time
                """)
                .executeUpdate();
    }

    private void createReservationIndex() {

        entityManager.createNativeQuery("""
                CREATE INDEX idx_reservation_member_date_time
                ON reservations (
                    member_id,
                    reservation_date,
                    reservation_time
                )
                """)
                .executeUpdate();
    }

    private void analyzeReservations() {

        entityManager.createNativeQuery("""
                ANALYZE reservations
                """)
                .executeUpdate();
    }

    private void printExecutionPlan(List<?> executionPlan) {

        executionPlan.forEach(
                line -> System.out.println(line.toString())
        );
    }

    private Member createTestData() {

        Member targetMember = null;

        LocalDate baseDate =
                LocalDate.of(2026, 1, 1);

        for (int memberIndex = 0;
             memberIndex < 10;
             memberIndex++) {

            Member member =
                    createMember(memberIndex);

            if (memberIndex == 0) {
                targetMember = member;
            }

            Vehicle vehicle =
                    createVehicle(member, memberIndex);

            for (int i = 0; i < 1_000; i++) {

                Reservation reservation =

                        Reservation.create(
                                member,
                                vehicle,
                                baseDate.plusDays(i % 365),
                                LocalTime.of(
                                        9 + (i % 9),
                                        0
                                )
                        );

                reservationRepository.save(

                        reservation

                );

                if (i > 0 && i % 500 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
        }

        entityManager.flush();
        entityManager.clear();

        return targetMember;
    }

    private Member createMember(int index) {

        return memberRepository.save(
                Member.create(
                        "PostgreSQL 인덱스 테스트 회원" + index,
                        "postgres-index-" + index + "@test.com",
                        "encoded-password"
                )
        );
    }

    private Vehicle createVehicle(
            Member member,
            int index
    ) {
        return vehicleRepository.save(

                Vehicle.create(
                        member,
                        "45하" + String.format("%04d", index),
                        "Mitsubishi",
                        "Lancer Evolution IX",
                        2006
                )
        );
    }
}
