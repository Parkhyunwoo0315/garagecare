package com.hyunu.garagecare.global.auth;

import com.hyunu.garagecare.member.service.MemberService;
import com.hyunu.garagecare.member.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AdminAuthorizationInterceptorTest {

    private MemberService memberService;
    private AdminAuthorizationInterceptor interceptor;

    @BeforeEach
    void setUp() {
        memberService = mock(MemberService.class);
        interceptor = new AdminAuthorizationInterceptor(memberService);
    }

    @Test
    @DisplayName("ADMIN 회원은 관리자 요청을 통과한다")
    void adminCanAccess() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Long memberId = 1L;

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SessionConst.LOGIN_MEMBER_ID))
                .thenReturn(memberId);
        when(memberService.isAdmin(memberId))
                .thenReturn(true);

        boolean result = interceptor.preHandle(
                request,
                response,
                new Object()
        );

        assertThat(result).isTrue();

        verify(memberService).isAdmin(memberId);
        verify(response, never()).sendError(anyInt());
    }

    @Test
    @DisplayName("MEMBER 회원은 관리자 요청이 거부된다")
    void memberCannotAccess() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Long memberId = 1L;

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SessionConst.LOGIN_MEMBER_ID))
                .thenReturn(memberId);
        when(memberService.isAdmin(memberId))
                .thenReturn(false);

        boolean result = interceptor.preHandle(
                request,
                response,
                new Object()
        );

        assertThat(result).isFalse();

        verify(memberService).isAdmin(memberId);
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("세션이 없으면 관리자 요청이 거부된다")
    void noSessionCannotAccess() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getSession(false)).thenReturn(null);

        boolean result = interceptor.preHandle(
                request,
                response,
                new Object()
        );

        assertThat(result).isFalse();

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        verifyNoInteractions(memberService);
    }

    @Test
    @DisplayName("세션에 로그인 회원 ID가 없으면 관리자 요청이 거부")
    void noLoginMemberCannotAccess() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SessionConst.LOGIN_MEMBER_ID))
                .thenReturn(null);

        boolean result = interceptor.preHandle(
                request,
                response,
                new Object()
        );

        assertThat(result).isFalse();

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        verifyNoInteractions(memberService);
    }
}
