
package com.hyunu.garagecare.mypage.controller;

import com.hyunu.garagecare.maintenance.domain.MaintenanceItem;
import com.hyunu.garagecare.maintenance.repository.MaintenanceItemRepository;
import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.member.session.SessionConst;
import com.hyunu.garagecare.reservation.dto.ReservationCreateRequest;
import com.hyunu.garagecare.reservation.service.ReservationService;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class MypageControllerTest {

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


    @Test
    @DisplayName("비로그인 사용자는 로그인 후 마이페이지로 복귀")
    void mypageWithoutLogin() throws Exception {

        MvcResult result = mockMvc.perform(
                        get("/mypage")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrlPattern(
                                "/members/login?redirectURL=*"
                        )
                )
                .andReturn();

        String redirectUrl =
                result.getResponse().getRedirectedUrl();

        assertThat(redirectUrl).isNotNull();

        URI uri = URI.create(redirectUrl);

        assertThat(uri.getPath())
                .isEqualTo("/members/login");

        String query = URLDecoder.decode(
                uri.getRawQuery(),
                StandardCharsets.UTF_8
        );

        assertThat(query)
                .contains("redirectURL=/mypage");
    }


    @Test
    @Transactional
    @DisplayName("마이페이지에는 로그인 회원의 차량과 예약 건수만 표시")
    void mypageShowsOnlyCurrentMemberData()
            throws Exception {

        // given
        Member memberA = memberRepository.save(
                Member.create(
                        "회원 A",
                        "mypage-member-a@test.com",
                        "password"
                )
        );

        Member memberB = memberRepository.save(
                Member.create(
                        "회원 B",
                        "mypage-member-b@test.com",
                        "password"
                )
        );

        Vehicle vehicleA = vehicleRepository.save(
                Vehicle.create(
                        memberA,
                        "12가3456",
                        "현대",
                        "아반떼",
                        2022
                )
        );

        Vehicle vehicleB = vehicleRepository.save(
                Vehicle.create(
                        memberB,
                        "78나9012",
                        "기아",
                        "K5",
                        2023
                )
        );

        MaintenanceItem item =
                maintenanceItemRepository.save(
                        MaintenanceItem.create(
                                "마이페이지 테스트 정비",
                                "예약 건수 확인용 정비 항목",
                                50000L
                        )
                );

        ReservationCreateRequest requestA =
                new ReservationCreateRequest();

        requestA.setVehicleId(vehicleA.getId());
        requestA.setReservationDate(
                LocalDate.now().plusDays(2)
        );
        requestA.setReservationTime(
                LocalTime.of(14, 0)
        );
        requestA.setMaintenanceItemIds(
                List.of(item.getId())
        );

        reservationService.createReservation(
                memberA.getId(),
                requestA
        );

        ReservationCreateRequest requestB =
                new ReservationCreateRequest();

        requestB.setVehicleId(vehicleB.getId());
        requestB.setReservationDate(
                LocalDate.now().plusDays(2)
        );
        requestB.setReservationTime(
                LocalTime.of(14, 0)
        );
        requestB.setMaintenanceItemIds(
                List.of(item.getId())
        );

        reservationService.createReservation(
                memberB.getId(),
                requestB
        );

        MockHttpSession session = new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                memberA.getId()
        );

        // when & then
        mockMvc.perform(
                        get("/mypage")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        view().name("mypage/index")
                )
                .andExpect(
                        model().attribute(
                                "vehicleCount",
                                1
                        )
                )
                .andExpect(
                        model().attribute(
                                "reservationCount",
                                1L
                        )
                );
    }
}
