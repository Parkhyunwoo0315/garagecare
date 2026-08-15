package com.hyunu.garagecare.reservation.controller;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.session.SessionConst;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservationControllerTest {

    @Autowired
    MockMvc mockMvc;

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
}
