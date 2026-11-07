package com.hyunu.garagecare.home.controller;

import com.hyunu.garagecare.member.session.SessionConst;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HomeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("비로그인 사용자는 메인 페이지에 접근 가능")
    void anonymousUserCanAccessHome() throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    @DisplayName("메인 페이지에 정비 서비스 안내 문구가 표시된다")
    void homeContainsServiceIntroduction() throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(
                        containsString("예약부터 방문까지")
                ))
                .andExpect(content().string(
                        containsString("정비 예약 신청")
                ));
    }

    @Test
    @DisplayName("메인 페이지에 기존 예약 기능으로 이동하는 링크가 존재")
    void homeContainsReservationLink() throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("/reservations/new")
                ));
    }

    @Test
    @DisplayName("메인 페이지에 실제 정비소 상호가 표시")
    void homeDisplaysShopName() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("동부자동차공업사")
                ));
    }

    @Test
    @DisplayName("메인 페이지에 정비소 연락처 표시 가능")
    void homeDisplaysContactInformation() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("02-2682-6264")
                ))
                .andExpect(content().string(
                        containsString("010-5273-8889")
                ))
                .andExpect(content().string(
                        containsString("02-2618-6265")
                ))
                .andExpect(content().string(
                        containsString("ppy0925@naver.com")
                ))
                .andExpect(content().string(
                        containsString("서울 구로구 고척로 31")
                ));
    }

    @Test
    @DisplayName("비로그인 사용자는 로그인과 회원가입 메뉴 보기 가능")
    void anonymousUserSeesLoginNavigation() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("href=\"/members/login\"")
                ))
                .andExpect(content().string(
                        containsString("href=\"/members/signup\"")
                ))
                .andExpect(content().string(
                        not(containsString("action=\"/members/logout\""))
                ));
    }

    @Test
    @DisplayName("로그인 사용자는 로그아웃 메뉴 보기 가능")
    void loggedInUserSeesLogoutNavigation() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_MEMBER_ID, 1L);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("action=\"/members/logout\"")
                ))
                .andExpect(content().string(
                        not(containsString("href=\"/members/login\""))
                ));
    }

    @Test
    @DisplayName("메인 페이지에서 예약 신청 화면으로 이동 가능")
    void homeContainsReservationCTA() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("href=\"/reservations/new\"")
                ));
    }

    @Test
    @DisplayName("공지사항과 별도의 오시는 길 섹션이 표시")
    void homeDisplaysNoticesAndLocation() throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("공지사항 및 문의")
                ))
                .andExpect(content().string(
                        containsString("공지사항 서비스를 준비하고 있습니다.")
                ))
                .andExpect(content().string(
                        containsString("id=\"location\"")
                ))
                .andExpect(content().string(
                        containsString("id=\"naver-map\"")
                ))
                .andExpect(content().string(
                        containsString("네이버 지도에서 위치 확인")
                ));
    }
}