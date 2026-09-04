package com.hyunu.garagecare.reservation.repository;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.reservation.domain.Reservation;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Transactional
class ReservationNPlusOneTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Test
    @DisplayName("예약 목록 조회 N+1 발생 여부 확인")
    void reservationListNPlusOne() {

        // given
        // 여기서 기존 ReservationServiceTest에서 사용한
        // 회원 / 차량 / 예약 생성 helper를 재사용해서
        // 예약 10건을 생성

        Member member = createMember();

        for (int i = 0; i < 10; i++) {

            Vehicle vehicle = createVehicle(member, i);

            createReservation(
                    member,
                    vehicle,
                    i
            );
        }

        entityManager.flush();
        entityManager.clear();

        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);

        Statistics statistics = sessionFactory.getStatistics();

        statistics.clear();

        // when
        List<Reservation> reservations = reservationRepository
                        .findAllByMemberIdOrderByReservationDateDescReservationTimeDesc(
                                member.getId()
                        );

        reservations.forEach(
                reservation ->
                        reservation.getVehicle()
                                .getVehicleNumber()
        );

        // then
        long queryCount =
                statistics.getPrepareStatementCount();

        assertThat(queryCount).isEqualTo(1);

        System.out.println(
                "===== Query Count: "
                        + queryCount
                        + " ====="
        );
    }

    private Member createMember() {
        Member member = Member.create(
                "N+1 테스트 회원",
                "nplusone@test.com",
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
                "10가" + String.format("%04d", index),
                " ",
                " ",
                2000
        );

        return vehicleRepository.save(vehicle);
    }

    private Reservation createReservation(
            Member member,
            Vehicle vehicle,
            int index
    ) {
        Reservation reservation = Reservation.create(
                member,
                vehicle,
                LocalDate.now().plusDays(index + 1),
                LocalTime.of(10, 0)
        );

        return reservationRepository.save(reservation);
    }
}