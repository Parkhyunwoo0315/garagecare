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
    @DisplayName("대기 중인 예약을 취소하면 상태가 CANCELED로 변경")
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
    @DisplayName("이미 취소된 예약을 다시 취소해도 CANCELED 상태가 유지")
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

    @Test
    @DisplayName("대기 중인 예약을 확정하면 상태가 CONFIRMED로 변경")
    void confirmPendingReservation() {

        // given
        Reservation reservation = createReservation();

        // when
        reservation.confirm();

        // then
        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("확정된 예약을 완료하면 상태가 COMPLETED로 변경")
    void completeConfirmedReservation() {

        // given
        Reservation reservation = createReservation();

        reservation.confirm();

        // when
        reservation.complete();

        // then
        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.COMPLETED);
    }

    @Test
    @DisplayName("취소된 예약은 확정할 수 없음")
    void cannotConfirmCanceledReservation() {

        // given
        Reservation reservation = createReservation();

        reservation.cancel();

        // when & then
        assertThatThrownBy(reservation::confirm)
                .isInstanceOf(InvalidReservationStatusException.class);

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("취소된 예약은 완료할 수 없음")
    void cannotCompleteCanceledReservation() {

        // given
        Reservation reservation = createReservation();

        reservation.cancel();

        // when & then
        assertThatThrownBy(reservation::complete)
                .isInstanceOf(InvalidReservationStatusException.class);

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("완료된 예약은 다시 확정할 수 없음")
    void cannotConfirmCompletedReservation() {

        // given
        Reservation reservation = createReservation();

        reservation.confirm();
        reservation.complete();

        // when & then
        assertThatThrownBy(reservation::confirm)
                .isInstanceOf(InvalidReservationStatusException.class);

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.COMPLETED);
    }

    @Test
    @DisplayName("대기 중인 예약은 바로 완료할 수 없음")
    void cannotCompletePendingReservation() {

        // given
        Reservation reservation = createReservation();

        // when & then
        assertThatThrownBy(reservation::complete)
                .isInstanceOf(InvalidReservationStatusException.class);

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.PENDING);
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
