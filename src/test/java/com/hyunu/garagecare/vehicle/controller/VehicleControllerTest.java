package com.hyunu.garagecare.vehicle.controller;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.member.session.SessionConst;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VehicleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Test
    @DisplayName("로그인 회원은 차량 목록 화면에 접근 가능")
    void vehicleList() throws Exception {

        // given
        Member member = createMember(
                "vehicle-controller-list@test.com"
        );

        MockHttpSession session =
                createSession(member);

        // when & then
        mockMvc.perform(
                        get("/vehicles")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        view().name("vehicle/list")
                )
                .andExpect(
                        model().attributeExists("vehicles")
                );
    }

    @Test
    @DisplayName("비로그인 사용자는 차량 목록에 접근 불가능")
    void vehicleListWithoutLogin() throws Exception {

        mockMvc.perform(get("/vehicles"))
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
    @DisplayName("로그인 회원은 차량 등록 화면에 접근 가능")
    void createForm() throws Exception {

        // given
        Member member = createMember(
                "vehicle-controller-form@test.com"
        );

        MockHttpSession session =
                createSession(member);

        // when & then
        mockMvc.perform(
                        get("/vehicles/new")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        view().name(
                                "vehicle/create-form"
                        )
                )
                .andExpect(
                        model().attributeExists("form")
                );
    }

    @Test
    @DisplayName("로그인 회원은 차량 등록 가능")
    void createVehicle() throws Exception {

        // given
        Member member = createMember(
                "vehicle-controller-create@test.com"
        );

        MockHttpSession session =
                createSession(member);

        // when & then
        mockMvc.perform(
                        post("/vehicles")
                                .session(session)
                                .param(
                                        "vehicleNumber",
                                        "12가3456"
                                )
                                .param(
                                        "manufacturer",
                                        "Mitsubishi"
                                )
                                .param(
                                        "model",
                                        "Lancer Evolution VI Tommi Mäkinen Edition"
                                )
                                .param(
                                        "modelYear",
                                        "2000"
                                )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl("/vehicles")
                );

        assertThat(
                vehicleRepository
                        .findAllByMemberId(
                                member.getId()
                        )
        ).hasSize(1);
    }


    @Test
    @DisplayName("로그인 회원은 차량 등록 화면에서 마이페이지와 로그아웃 메뉴 표시")
    void vehicleCreateFormShowsLoggedInHeader()
            throws Exception {

        // given
        MockHttpSession session = new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                1L
        );

        // when & then
        mockMvc.perform(
                        get("/vehicles/new")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        model().attribute("loggedIn", true)
                )
                .andExpect(
                        content().string(
                                containsString("header-logout")
                        )
                )
                .andExpect(
                        content().string(
                                not(containsString("header-signup"))
                        )
                );
    }

    @Test
    @DisplayName("비로그인 사용자는 홈페이지에서 로그인과 회원가입 메뉴 표시")
    void homeShowsLoggedOutHeader()
            throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(
                        model().attribute("loggedIn", false)
                )
                .andExpect(
                        content().string(
                                containsString("header-signup")
                        )
                )
                .andExpect(
                        content().string(
                                not(containsString("header-logout"))
                        )
                );
    }


    private Member createMember(String email) {
        return memberRepository.save(
                Member.create(
                        "테스트 회원",
                        email,
                        "password"
                )
        );
    }

    private MockHttpSession createSession(
            Member member
    ) {
        MockHttpSession session =
                new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                member.getId()
        );

        return session;
    }
}
