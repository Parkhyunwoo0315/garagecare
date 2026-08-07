package com.hyunu.garagecare.global.auth;

import com.hyunu.garagecare.member.session.SessionConst;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.*;

class LoginCheckInterceptorTest {

    private final LoginCheckInterceptor interceptor = new LoginCheckInterceptor();

    @Test
    @DisplayName("로그인한 회원은 보호된 요청을 통과")
    void authenticatedMemberCanAccess() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();

        session.setAttribute(SessionConst.LOGIN_MEMBER_ID, 1L);
        request.setSession(session);
        request.setRequestURI("/reservations");

        //when
        boolean result = interceptor.preHandle(
                request,
                response,
                new Object()
        );

        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("세션이 없는 사용자는 로그안 화면으로 이동")
    void noSessionRedirectsToLogin() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setRequestURI("/reservations");

        //when
        boolean result = interceptor.preHandle(
                request,
                response,
                new Object()
        );

        //then
        assertThat(result).isFalse();
        assertThat(response.getRedirectedUrl())
                .startsWith("/members/login?redirectURL=");
    }

    @Test
    @DisplayName("세션은 존재하지만 로그인 정보가 없으면 접근 불가능")
    void sessionWithoutLoginMemberRedirects() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();

        request.setSession(session);
        request.setRequestURI("/reservations");

        //when
        boolean result = interceptor.preHandle(
                request,
                response,
                new Object()
        );

        //then
        assertThat(result).isFalse();
        assertThat(response.getRedirectedUrl())
                .startsWith("/members/login");
    }

    @Test
    @DisplayName("로그인 전 요청의 쿼리 파라미터도 유지")
    void preserveQueryString() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setRequestURI("/reservations");
        request.setQueryString("page=2");

        //when
        boolean result = interceptor.preHandle(
                request,
                response,
                new Object()
        );

        //then
        assertThat(result).isFalse();
        assertThat(response.getRedirectedUrl())
                .contains("redirectURL=");
    }
}
