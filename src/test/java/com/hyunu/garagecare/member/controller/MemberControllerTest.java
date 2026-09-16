package com.hyunu.garagecare.member.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import com.hyunu.garagecare.member.dto.MemberSignUpRequest;
import com.hyunu.garagecare.member.service.MemberService;
import com.hyunu.garagecare.member.session.SessionConst;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("회원가입 화면 조회")
    void signupForm() throws Exception {
        mockMvc.perform(get("/members/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/signup-form"));
    }

    @Test
    @DisplayName("로그인 화면에 기존 요청 URL을 전달")
    void loginForm() throws Exception {
        mockMvc.perform(get("/members/login")
                        .param("redirectURL", "/reservations"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/login-form"))
                .andExpect(model().attribute(
                        "redirectURL",
                        "/reservations")
                );
    }

    @Test
    @Transactional
    @DisplayName("로그인 성공 후 기본 경로는 차량 목록으로 이동")
    void loginSuccess() throws Exception {

        // given
        signUpMember(
                "login-default@test.com",
                "password123"
        );

        // when & then
        mockMvc.perform(
                        post("/members/login")
                                .param(
                                        "email",
                                        "login-default@test.com"
                                )
                                .param(
                                        "password",
                                        "password123"
                                )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl("/vehicles")
                )
                .andExpect(
                        request().sessionAttribute(
                                SessionConst.LOGIN_MEMBER_ID,
                                org.hamcrest.Matchers.notNullValue()
                        )
                );
    }

    @Test
    @Transactional
    @DisplayName("로그인 성공 후 기존 요청 URL로 복귀")
    void loginSuccessWithRedirectURL() throws Exception {

        // given
        signUpMember(
                "login-redirect@test.com",
                "password123"
        );

        // when & then
        mockMvc.perform(
                        post("/members/login")
                                .param(
                                        "email",
                                        "login-redirect@test.com"
                                )
                                .param(
                                        "password",
                                        "password123"
                                )
                                .param(
                                        "redirectURL",
                                        "/reservations/new"
                                )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl(
                                "/reservations/new"
                        )
                );
    }

    @Test
    @Transactional
    @DisplayName("외부 URL로 로그인 리다이렉트할 수 없다")
    void loginRejectsExternalRedirectURL() throws Exception {

        // given
        signUpMember(
                "login-external@test.com",
                "password123"
        );

        // when & then
        mockMvc.perform(
                        post("/members/login")
                                .param(
                                        "email",
                                        "login-external@test.com"
                                )
                                .param(
                                        "password",
                                        "password123"
                                )
                                .param(
                                        "redirectURL",
                                        "https://example.com"
                                )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl("/vehicles")
                );
    }

    @Test
    @DisplayName("로그아웃 후 로그인 화면으로 이동")
    void logout() throws Exception {

        mockMvc.perform(
                        post("/members/logout")
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl(
                                "/members/login"
                        )
                );
    }

    private void signUpMember(
            String email,
            String password
    ) {

        MemberSignUpRequest request =
                new MemberSignUpRequest();

        request.setName("테스트 회원");
        request.setEmail(email);
        request.setPassword(password);

        memberService.signUp(request);
    }
}