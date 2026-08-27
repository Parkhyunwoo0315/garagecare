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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/*
 * H2 환경에서 Reservation 인덱스 실행계획을 확인하기 위한 성능 실험 테스트.
 * 운영 DBMS 전환 후 동일 조건으로 재검증한다.
 */

@SpringBootTest
@Transactional
public class ReservationIndexPerformanceTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    ReservationRepository reservationRepository;

//    @Test
//    @DisplayName("예약 목록 조회 인덱스 적용 전 실행계획 확인")
//    void reservationIndexBeforeTest() {
//
//        //given
//        Member member = createMember();
//
//        createReservations(member);
//
//        printIndexes();
//
//        //when
//
//        String executionPlan = getExecutionPlan(member.getId());
//
//        //then
//        System.out.println("===== BEFORE EXECUTION PLAN =====");
//
//        System.out.println(executionPlan);
//    }

    @Test
    @DisplayName("예약 목록 조회 인덱스 적용 후 실행계획 확인")
    void reservationIndexAfterTest() {

        //given
        Member member = createTestData();

        entityManager
                .createNativeQuery("ANALYZE")
                .executeUpdate();

        printIndexes();

        //when

        String executionPlan = getExecutionPlan(member.getId());

        //then
        System.out.println("===== AFTER EXECUTION PLAN =====");

        System.out.println(executionPlan);
    }

    private String getExecutionPlan(Long memberId) {

        Object result =
                entityManager
                        .createNativeQuery("""
                            EXPLAIN
                            SELECT *
                            FROM reservations
                            WHERE member_id = :memberId
                            ORDER BY reservation_date DESC,
                                     reservation_time DESC
                            """)
                        .setParameter("memberId", memberId)
                        .getSingleResult();

        return result.toString();
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
                    createVehicle(
                            member,
                            memberIndex
                    );

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

        Member member = Member.create(
                "인덱스 테스트 회원" + index,
                "index" + index + "@test.com",
                "encoded-password"
        );

        return memberRepository.save(member);
    }

    private Vehicle createVehicle(
            Member member,
            int index
    ) {

        Vehicle vehicle = Vehicle.create(
                member,
                "99가" + String.format("%04d", index),
                "Porsche",
                "911 GT3 (992)",
                2022
        );

        return vehicleRepository.save(vehicle);
    }

    private void createReservations(
            Member member
    ) {

        Vehicle vehicle =
                createVehicle(member, 0);

        LocalDate baseDate =
                LocalDate.of(2026, 1, 1);

        for (int i = 0; i < 10_000; i++) {

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

        entityManager.flush();
        entityManager.clear();
    }

    private void printIndexes() {

        List<?> indexes =
                entityManager
                        .createNativeQuery("""
                            SELECT
                                INDEX_NAME,
                                COLUMN_NAME
                            FROM INFORMATION_SCHEMA.INDEX_COLUMNS
                            WHERE TABLE_NAME = 'RESERVATIONS'
                            ORDER BY
                                INDEX_NAME,
                                ORDINAL_POSITION
                            """)
                        .getResultList();

        System.out.println("===== RESERVATION INDEXES =====");

        if (indexes.isEmpty()) {
            System.out.println("No indexes found.");

            return;
        }

        for (Object index : indexes) {
            Object[] row = (Object[]) index;

            System.out.println("Index: " + row[0] + " | Column: " + row[1]);
        }
    }
}
