package com.hyunu.garagecare.admin.vehicle.controller;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.member.session.SessionConst;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminVehicleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    @DisplayName("ADMIN은 관리자 차량 목록 화면에 접근 가능")
    void adminCanAccessVehicleList() throws Exception {

        // given
        Member admin = createAdmin(
                "차량 관리자",
                "admin-vehicle-controller@test.com"
        );

        MockHttpSession session = createSession(
                admin.getId()
        );

        // when & then
        mockMvc.perform(
                        get("/admin/vehicles")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        view().name("admin/vehicle/list")
                )
                .andExpect(
                        model().attributeExists("vehicles")
                );
    }

    @Test
    @DisplayName("ADMIN은 관리자 차량 상세 화면에 접근 가능")
    void adminCanAccessVehicleDetail() throws Exception {

        // given
        Member admin = createAdmin(
                "차량 상세 관리자",
                "admin-vehicle-detail-controller@test.com"
        );

        Member member = memberRepository.save(
                Member.create(
                        "차량 소유 회원",
                        "vehicle-owner-controller@test.com",
                        "password"
                )
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        member,
                        "12가3456",
                        "BMW",
                        "M3 (E46)",
                        2003
                )
        );

        MockHttpSession session = createSession(
                admin.getId()
        );

        // when & then
        mockMvc.perform(
                        get(
                                "/admin/vehicles/{vehicleId}",
                                vehicle.getId()
                        )
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        view().name("admin/vehicle/detail")
                )
                .andExpect(
                        model().attributeExists("vehicle")
                );
    }

    @Test
    @DisplayName("MEMBER는 관리자 차량 목록에 접근 불가능")
    void memberCannotAccessVehicleList() throws Exception {

        // given
        Member member = memberRepository.save(
                Member.create(
                        "일반 회원",
                        "member-vehicle-access@test.com",
                        "password"
                )
        );

        MockHttpSession session = createSession(
                member.getId()
        );

        // when & then
        mockMvc.perform(
                        get("/admin/vehicles")
                                .session(session)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MEMBER는 관리자 차량 상세 화면에 접근 불가능")
    void memberCannotAccessVehicleDetail() throws Exception {

        // given
        Member member = memberRepository.save(
                Member.create(
                        "일반 회원",
                        "member-vehicle-detail-access@test.com",
                        "password"
                )
        );

        MockHttpSession session = createSession(
                member.getId()
        );

        // when & then
        mockMvc.perform(
                        get("/admin/vehicles/1")
                                .session(session)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("비로그인 사용자는 관리자 차량 목록 접근 시 로그인 화면으로 이동")
    void unauthenticatedUserCannotAccessVehicleList() throws Exception {

        mockMvc.perform(
                        get("/admin/vehicles")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrlPattern(
                                "/members/login?redirectURL=*"
                        )
                );
    }

    @Test
    @DisplayName("비로그인 사용자는 관리자 차량 상세 접근 시 로그인 화면으로 이동")
    void unauthenticatedUserCannotAccessVehicleDetail() throws Exception {

        mockMvc.perform(
                        get("/admin/vehicles/1")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrlPattern(
                                "/members/login?redirectURL=*"
                        )
                );
    }

    private MockHttpSession createSession(
            Long memberId
    ) {
        MockHttpSession session =
                new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                memberId
        );

        return session;
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
                .setParameter(
                        "id",
                        member.getId()
                )
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        return memberRepository
                .findById(member.getId())
                .orElseThrow();
    }
}