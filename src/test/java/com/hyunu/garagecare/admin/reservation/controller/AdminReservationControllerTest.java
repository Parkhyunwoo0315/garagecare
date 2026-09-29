package com.hyunu.garagecare.admin.reservation.controller;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.member.session.SessionConst;
import com.hyunu.garagecare.reservation.domain.Reservation;
import com.hyunu.garagecare.reservation.domain.ReservationStatus;
import com.hyunu.garagecare.reservation.repository.ReservationRepository;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminReservationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Test
    @DisplayName("ADMIN은 관리자 예약 목록 화면에 접근 가능")
    void adminCanAccessReservationList() throws Exception {

        // given
        Member admin = createAdmin(
                "관리자",
                "admin-reservation@test.com"
        );

        MockHttpSession session = new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                admin.getId()
        );

        // when & then
        mockMvc.perform(
                        get("/admin/reservations")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        view().name("admin/reservation/list")
                )
                .andExpect(
                        model().attributeExists("reservations")
                );
    }

    @Test
    @DisplayName("MEMBER는 관리자 예약 목록에 접근 불가능")
    void memberCannotAccessReservationList() throws Exception {

        // given
        Member member = memberRepository.save(
                Member.create(
                        "일반회원",
                        "member-reservation@test.com",
                        "password"
                )
        );

        MockHttpSession session = new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                member.getId()
        );

        // when & then
        mockMvc.perform(
                        get("/admin/reservations")
                                .session(session)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("비로그인 사용자는 관리자 예약 목록 접근 시 로그인 화면으로 이동")
    void unauthenticatedUserCannotAccessReservationList() throws Exception {

        mockMvc.perform(
                        get("/admin/reservations")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrlPattern(
                                "/members/login?redirectURL=*"
                        )
                );
    }

    @Test
    @DisplayName("ADMIN은 대기 중인 예약을 확정 가능")
    void adminCanConfirmReservation() throws Exception {

        Member admin = createAdmin(
                "예약 확정 관리자",
                "admin-confirm-controller@test.com"
        );

        Member member = memberRepository.save(
                Member.create(
                        "예약 회원",
                        "member-confirm-controller@test.com",
                        "password"
                )
        );

        Reservation reservation = createReservation(
                member,
                "11가1111"
        );

        MockHttpSession session = new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                admin.getId()
        );

        mockMvc.perform(
                        post(
                                "/admin/reservations/{reservationId}/confirm",
                                reservation.getId()
                        )
                                .session(session)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl(
                                "/admin/reservations/" + reservation.getId()
                        )
                );

        entityManager.flush();
        entityManager.clear();

        Reservation updatedReservation =
                reservationRepository
                        .findById(reservation.getId())
                        .orElseThrow();

        assertThat(updatedReservation.getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("ADMIN은 확정된 예약을 완료 가능")
    void adminCanCompleteReservation() throws Exception {

        Member admin = createAdmin(
                "예약 완료 관리자",
                "admin-complete-controller@test.com"
        );

        Member member = memberRepository.save(
                Member.create(
                        "완료 예약 회원",
                        "member-complete-controller@test.com",
                        "password"
                )
        );

        Reservation reservation = createReservation(
                member,
                "22나2222"
        );

        reservation.confirm();

        entityManager.flush();
        entityManager.clear();

        MockHttpSession session = new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                admin.getId()
        );

        mockMvc.perform(
                        post(
                                "/admin/reservations/{reservationId}/complete",
                                reservation.getId()
                        )
                                .session(session)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl(
                                "/admin/reservations/" + reservation.getId()
                        )
                );

        entityManager.flush();
        entityManager.clear();

        Reservation updatedReservation =
                reservationRepository
                        .findById(reservation.getId())
                        .orElseThrow();

        assertThat(updatedReservation.getStatus())
                .isEqualTo(ReservationStatus.COMPLETED);
    }

    @Test
    @DisplayName("MEMBER는 관리자 예약 확정 기능에 접근 불가능")
    void memberCannotConfirmReservation() throws Exception {

        Member member = memberRepository.save(
                Member.create(
                        "일반 회원",
                        "member-cannot-confirm@test.com",
                        "password"
                )
        );

        Reservation reservation = createReservation(
                member,
                "33다3333"
        );

        MockHttpSession session = new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                member.getId()
        );

        mockMvc.perform(
                        post(
                                "/admin/reservations/{reservationId}/confirm",
                                reservation.getId()
                        )
                                .session(session)
                )
                .andExpect(status().isForbidden());

        entityManager.flush();
        entityManager.clear();

        Reservation unchangedReservation =
                reservationRepository
                        .findById(reservation.getId())
                        .orElseThrow();

        assertThat(unchangedReservation.getStatus())
                .isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("MEMBER는 관리자 예약 완료 기능에 접근 불가능")
    void memberCannotCompleteReservation() throws Exception {

        Member member = memberRepository.save(
                Member.create(
                        "일반 완료 회원",
                        "member-cannot-complete@test.com",
                        "password"
                )
        );

        Reservation reservation = createReservation(
                member,
                "44라4444"
        );

        reservation.confirm();

        entityManager.flush();
        entityManager.clear();

        MockHttpSession session = new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                member.getId()
        );

        mockMvc.perform(
                        post(
                                "/admin/reservations/{reservationId}/complete",
                                reservation.getId()
                        )
                                .session(session)
                )
                .andExpect(status().isForbidden());

        entityManager.flush();
        entityManager.clear();

        Reservation unchangedReservation =
                reservationRepository
                        .findById(reservation.getId())
                        .orElseThrow();

        assertThat(unchangedReservation.getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);
    }

    private Reservation createReservation(
            Member member,
            String vehicleNumber
    ) {
        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        member,
                        vehicleNumber,
                        "Porsche",
                        "911 GT3",
                        2022
                )
        );

        return reservationRepository.save(
                Reservation.create(
                        member,
                        vehicle,
                        java.time.LocalDate.now().plusDays(1),
                        java.time.LocalTime.of(14, 0)
                )
        );
    }

    private Member createAdmin(
            String name,
            String email
    ) {
        Member member = memberRepository.save(
                Member.create(
                        name,
                        email,
                        "password"
                )
        );

        entityManager.createNativeQuery(
                        "update members set role = 'ADMIN' where id = :id"
                )
                .setParameter("id", member.getId())
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        return memberRepository
                .findById(member.getId())
                .orElseThrow();
    }
}
