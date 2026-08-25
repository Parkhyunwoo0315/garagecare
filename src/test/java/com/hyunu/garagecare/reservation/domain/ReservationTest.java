package com.hyunu.garagecare.reservation.domain;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.reservation.exception.InvalidReservationStatusException;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

class ReservationTest {

    @Test
    @DisplayName("대기 중인 예약을 취소하면 상태가 CANCELED로 변경된다")
    void cancelPendingReservation() {

        // given
        Reservation reservation = createReservation();

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.PENDING);

        // when
        reservation.cancel();

        // then
        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("이미 취소된 예약을 다시 취소해도 CANCELED 상태가 유지된다")
    void cancelAlreadyCanceledReservation() {

        // given
        Reservation reservation = createReservation();

        reservation.cancel();

        // when
        reservation.cancel();

        // then
        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
    }

    private Reservation createReservation() {

        Member member = Member.create(
                "홍길동",
                "domain@test.com",
                "password"
        );

        Vehicle vehicle = Vehicle.create(
                member,
                "00아0000",
                "Mercedes-Benz",
                "E250",
                2022
        );

        return Reservation.create(
                member,
                vehicle,
                LocalDate.now().plusDays(1),
                LocalTime.of(14, 0)
        );
    }
}
