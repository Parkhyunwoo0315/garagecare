package com.hyunu.garagecare.global.auth;

import com.hyunu.garagecare.member.service.MemberService;
import com.hyunu.garagecare.member.session.SessionConst;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminAuthorizationWebTest {

    private MockMvc mockMvc;
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = mock(MemberService.class);

        LoginCheckInterceptor loginCheckInterceptor =
                new LoginCheckInterceptor();

        AdminAuthorizationInterceptor adminAuthorizationInterceptor =
                new AdminAuthorizationInterceptor(memberService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestAdminController())
                .addInterceptors(
                        loginCheckInterceptor,
                        adminAuthorizationInterceptor
                )
                .build();
    }

    @Test
    @DisplayName("비로그인 사용자가 관리자 영역에 접근하면 로그인 페이지로 이동")
    void unauthenticatedUserRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/admin/test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(
                        "/members/login?redirectURL=%2Fadmin%2Ftest"
                ));
    }

    @Test
    @DisplayName("MEMBER가 관리자 영역에 접근하면 403 응답을 반환")
    void memberCannotAccessAdmin() throws Exception {
        Long memberId = 1L;

        when(memberService.isAdmin(memberId))
                .thenReturn(false);

        mockMvc.perform(
                        get("/admin/test")
                                .sessionAttr(
                                        SessionConst.LOGIN_MEMBER_ID,
                                        memberId
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN은 관리자 영역에 접근 가능")
    void adminCanAccessAdmin() throws Exception {
        Long memberId = 1L;

        when(memberService.isAdmin(memberId))
                .thenReturn(true);

        mockMvc.perform(
                        get("/admin/test")
                                .sessionAttr(
                                        SessionConst.LOGIN_MEMBER_ID,
                                        memberId
                                )
                )
                .andExpect(status().isOk())
                .andExpect(content().string("admin"));
    }

    @RestController
    static class TestAdminController {

        @GetMapping("/admin/test")
        String admin() {
            return "admin";
        }
    }
}
