package com.hyunu.garagecare.reservation.repository;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.domain.MemberRole;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.reservation.domain.Reservation;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ReservationRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Test
    @DisplayName("회원의 예약 목록을 첫 페이지로 조회")
    void findReservationsFirstPage() {

        // given
        Member member = createMember();

        Vehicle vehicle = createVehicle(member);

        createReservations(
                member,
                vehicle,
                15
        );

        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by(
                        Sort.Order.desc("reservationDate"),
                        Sort.Order.desc("reservationTime")
                )
        );

        // when
        Page<Reservation> result =
                reservationRepository.findByMemberId(
                        member.getId(),
                        pageable
                );

        // then
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(15);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);

        assertThat(result.hasPrevious()).isFalse();
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("회원의 예약 목록을 두 번째 페이지로 조회")
    void findReservationsSecondPage() {

        // given
        Member member = createMember();

        Vehicle vehicle = createVehicle(member);

        createReservations(
                member,
                vehicle,
                15
        );

        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(
                1,
                10,
                Sort.by(
                        Sort.Order.desc("reservationDate"),
                        Sort.Order.desc("reservationTime")
                )
        );

        // when
        Page<Reservation> result =
                reservationRepository.findByMemberId(
                        member.getId(),
                        pageable
                );

        // then
        assertThat(result.getContent()).hasSize(5);

        assertThat(result.getTotalElements()).isEqualTo(15);
        assertThat(result.getTotalPages()).isEqualTo(2);

        assertThat(result.getNumber()).isEqualTo(1);

        assertThat(result.hasPrevious()).isTrue();
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("예약 목록은 예약 날짜와 시간의 내림차순으로 조회")
    void findReservationsOrderByDateAndTimeDesc() {

        // given
        Member member = createMember();

        Vehicle vehicle = createVehicle(member);

        createReservation(
                member,
                vehicle,
                LocalDate.of(2026, 9, 1),
                LocalTime.of(10, 0)
        );

        createReservation(
                member,
                vehicle,
                LocalDate.of(2026, 9, 3),
                LocalTime.of(14, 0)
        );

        createReservation(
                member,
                vehicle,
                LocalDate.of(2026, 9, 2),
                LocalTime.of(12, 0)
        );

        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by(
                        Sort.Order.desc("reservationDate"),
                        Sort.Order.desc("reservationTime")
                )
        );

        // when
        Page<Reservation> result =
                reservationRepository.findByMemberId(
                        member.getId(),
                        pageable
                );

        // then
        assertThat(result.getContent())
                .extracting(Reservation::getReservationDate)
                .containsExactly(
                        LocalDate.of(2026, 9, 3),
                        LocalDate.of(2026, 9, 2),
                        LocalDate.of(2026, 9, 1)
                );
    }

    @Test
    @DisplayName("다른 회원의 예약은 조회 불가능")
    void findReservationsOnlyForMember() {

        // given
        Member memberA = createMember(
                "memberA@test.com"
        );

        Member memberB = createMember(
                "memberB@test.com"
        );

        Vehicle vehicleA = createVehicle(
                memberA,
                "12가3456"
        );

        Vehicle vehicleB = createVehicle(
                memberB,
                "34나5678"
        );

        createReservations(
                memberA,
                vehicleA,
                15
        );

        createReservations(
                memberB,
                vehicleB,
                10
        );

        entityManager.flush();
        entityManager.clear();

        Pageable pageable =
                PageRequest.of(
                        0,
                        10
                );

        // when
        Page<Reservation> result =
                reservationRepository.findByMemberId(
                        memberA.getId(),
                        pageable
                );

        // then
        assertThat(result.getTotalElements()).isEqualTo(15);

        assertThat(result.getContent())
                .allMatch(
                        reservation ->
                                reservation
                                        .getMember()
                                        .getId()
                                        .equals(memberA.getId())
                );
    }

    @Test
    @DisplayName("예약이 없는 회원의 예약 목록은 빈 페이지 반환")
    void findEmptyReservationPage() {

        // given
        Member member = createMember();

        Pageable pageable =
                PageRequest.of(
                        0,
                        10
                );

        // when
        Page<Reservation> result =
                reservationRepository.findByMemberId(
                        member.getId(),
                        pageable
                );

        // then
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
    }

    private Member createMember() {

        return createMember(
                1
        );
    }

    private Member createMember(
            int index
    ) {

        Member member = Member.create(
                "페이지네이션 테스트 회원" + index,
                "pagination" + index + "@test.com",
                "encoded-password"
        );

        return memberRepository.save(member);
    }

    private Member createMember(
            String email
    ) {

        Member member = Member.create(
                "페이지네이션 테스트 회원",
                email,
                "encoded-password"
        );

        return memberRepository.save(member);
    }

    private Vehicle createVehicle(
            Member member
    ) {

        return createVehicle(
                member,
                1
        );
    }

    private Vehicle createVehicle(
            Member member,
            int index
    ) {

        Vehicle vehicle = Vehicle.create(
                member,
                "99가" + String.format("%04d", index),
                "BMW",
                "M3(E46)",
                2004
        );

        return vehicleRepository.save(vehicle);
    }

    private Vehicle createVehicle(
            Member member,
            String vehicleNumber
    ) {

        Vehicle vehicle = Vehicle.create(
                member,
                vehicleNumber,
                "BMW",
                "M3(E46)",
                2004
        );

        return vehicleRepository.save(vehicle);
    }

    private Reservation createReservation(
            Member member,
            Vehicle vehicle,
            LocalDate reservationDate,
            LocalTime reservationTime
    ) {

        Reservation reservation = Reservation.create(
                member,
                vehicle,
                reservationDate,
                reservationTime
        );

        return reservationRepository.save(
                reservation
        );
    }

    private void createReservations(
            Member member,
            Vehicle vehicle,
            int count
    ) {

        LocalDate baseDate =
                LocalDate.of(
                        2026,
                        9,
                        1
                );

        for (int i = 0; i < count; i++) {

            createReservation(
                    member,
                    vehicle,
                    baseDate.plusDays(i),
                    LocalTime.of(10, 0)
            );
        }
    }
}
