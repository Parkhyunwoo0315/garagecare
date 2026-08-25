package com.hyunu.garagecare.reservation.controller;

import com.hyunu.garagecare.maintenance.domain.MaintenanceItem;
import com.hyunu.garagecare.maintenance.repository.MaintenanceItemRepository;
import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.member.session.SessionConst;
import com.hyunu.garagecare.reservation.domain.Reservation;
import com.hyunu.garagecare.reservation.domain.ReservationStatus;
import com.hyunu.garagecare.reservation.dto.ReservationCreateRequest;
import com.hyunu.garagecare.reservation.repository.ReservationRepository;
import com.hyunu.garagecare.reservation.service.ReservationService;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    MaintenanceItemRepository maintenanceItemRepository;

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationRepository reservationRepository;

    @Test
    @DisplayName("로그인한 사용자는 예약 등록 화면에 접근 가능")
    void createForm() throws Exception {

        //given
        MockHttpSession session = new MockHttpSession();

        session.setAttribute(SessionConst.LOGIN_MEMBER_ID, 1L);

        //when & then
        mockMvc.perform(get("/reservations/new").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/create-form"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeExists("vehicles"))
                .andExpect(model().attributeExists("maintenanceItems"));

    }

    @Test
    @DisplayName("비로그인 사용자는 예약 등록 화면에 접근 불가능")
    void createFormWithoutLogin() throws Exception {

        mockMvc.perform(get("/reservations/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/members/login?redirectURL=*"));
    }

    @Test
    @DisplayName("로그인한 회원은 예약 목록 화면 조회 가능")
    @Transactional
    void reservationList() throws Exception {

        // given
        Member member = memberRepository.save(
                Member.create(
                        "홍길동",
                        "controller-list@test.com",
                        "password"
                )
        );

        MockHttpSession session =
                new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                member.getId()
        );

        // when & then
        mockMvc.perform(
                        get("/reservations")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        view().name("reservation/list")
                )
                .andExpect(
                        model().attributeExists("reservations")
                );
    }

    @Test
    @DisplayName("비로그인 사용자는 예약 목록에 접근 불가능")
    void reservationListWithoutLogin() throws Exception {

        mockMvc.perform(get("/reservations"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/members/login?redirectURL=*"));
    }

    @Test
    @DisplayName("로그인한 회원은 본인의 에약 상세 화면 조회 가능")
    void reservationDetail() throws Exception {

        //given
        Member member = memberRepository.save(
                Member.create(
                        "박현우",
                        "controller-detail@test.com",
                        "password"
                )
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        member,
                        "00차0000",
                        "기아",
                        "K5 DL3",
                        2021
                )
        );

        MaintenanceItem maintenanceItem = maintenanceItemRepository.save(
                MaintenanceItem.create(
                        "엔진오일 교환",
                        "엔진오일을 교환하고 싶습니다.",
                        70000L
                )
        );

        ReservationCreateRequest request = createRequest(
                vehicle.getId(),
                List.of(maintenanceItem.getId())
        );

        Long reservationId = reservationService.createReservation(
                member.getId(),
                request
        );

        MockHttpSession session =
                new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                member.getId()
        );

        //when & then
        mockMvc.perform(
                        get("/reservations/{reservationId}",
                                reservationId)
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/detail"))
                .andExpect(model().attributeExists("reservation"));

    }

    @Test
    @DisplayName("비로그인 사용자는 예약 상세 화면에 접근 불가능")
    void reservationDetailWithoutLogin() throws Exception {
        mockMvc.perform(
                get("/reservations/{reservationId}", 1L)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/members/login?redirectURL=*"));
    }

    @Test
    @Transactional
    @DisplayName("로그인한 회원이 본인의 예약을 취소하면 상세 화면으로 리다이렉트")
    void cancelReservation() throws Exception {

        // given
        Member member = memberRepository.save(
                Member.create(
                        "홍길동",
                        "controller-cancel@test.com",
                        "password"
                )
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        member,
                        "78타7878",
                        "Toyota",
                        "Supra RZ (A80)",
                        1998
                )
        );

        MaintenanceItem maintenanceItem = maintenanceItemRepository.save(
                MaintenanceItem.create(
                        "엔진오일 교환",
                        "엔진오일을 교환합니다.",
                        70000L
                )
        );

        Long reservationId =
                reservationService.createReservation(
                        member.getId(),
                        createRequest(
                                vehicle.getId(),
                                List.of(
                                        maintenanceItem.getId()
                                )
                        )
                );

        MockHttpSession session =
                new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                member.getId()
        );

        // when & then
        mockMvc.perform(
                        post(
                                "/reservations/{reservationId}/cancel",
                                reservationId
                        )
                                .session(session)
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl(
                                "/reservations/" + reservationId
                        )
                );

        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow();

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("비로그인 사용자는 예약을 취소 불가능")
    void cancelReservationWithoutLogin()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/reservations/{reservationId}/cancel",
                                1L
                        )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrlPattern(
                                "/members/login?redirectURL=*"
                        )
                );
    }

    @Test
    @Transactional
    @DisplayName("다른 회원의 예약 취소 요청은 차단")
    void cancelAnotherMembersReservation()
            throws Exception {

        // given
        Member owner = memberRepository.save(
                Member.create(
                        "예약 소유자",
                        "controller-owner@test.com",
                        "password"
                )
        );

        Member otherMember = memberRepository.save(
                Member.create(
                        "다른 회원",
                        "controller-other@test.com",
                        "password"
                )
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        owner,
                        "90파9090",
                        "Ford",
                        "Mustang Shelby GT350",
                        2017
                )
        );

        MaintenanceItem maintenanceItem = maintenanceItemRepository.save(
                        MaintenanceItem.create(
                                "타이어 점검",
                                "타이어를 점검하고 싶습니다.",
                                10000L
                        )
        );

        Long reservationId = reservationService.createReservation(
                        owner.getId(),
                        createRequest(
                                vehicle.getId(),
                                List.of(
                                        maintenanceItem.getId()
                                )
                        )
        );

        MockHttpSession session =
                new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                otherMember.getId()
        );

        // when & then
        assertThatThrownBy(
                () -> mockMvc.perform(
                        post(
                                "/reservations/{reservationId}/cancel",
                                reservationId
                        )
                                .session(session)
                )
        );
    }

    private ReservationCreateRequest createRequest(
            Long vehicleId,
            List<Long> maintenanceItemIds
    ) {
        ReservationCreateRequest request = new ReservationCreateRequest();

        request.setVehicleId(vehicleId);
        request.setReservationDate(LocalDate.now().plusDays(1));
        request.setReservationTime(LocalTime.of(14, 0));
        request.setMaintenanceItemIds(maintenanceItemIds);

        return request;
    }
}
