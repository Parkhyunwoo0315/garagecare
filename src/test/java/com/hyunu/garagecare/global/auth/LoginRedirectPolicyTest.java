package com.hyunu.garagecare.global.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRedirectPolicyTest {

    @Test
    @DisplayName("안전한 내부 경로는 그대로 반환")
    void safeInternalPath() {
        String result = LoginRedirectPolicy.resolve(
                "/reservations/new"
        );

        assertThat(result).isEqualTo("/reservations/new");
    }

    @Test
    @DisplayName("안전한 GET 쿼리 파라미터를 유지")
    void safePathWithQuery() {
        String result = LoginRedirectPolicy.resolve(
                "/reservations?page=1"
        );

        assertThat(result).isEqualTo("/reservations?page=1");
    }

    @Test
    @DisplayName("외부 절대 URL은 차단")
    void externalUrlBlocked() {
        String result = LoginRedirectPolicy.resolve(
                "https://example.com"
        );

        assertThat(result).isEqualTo("/vehicles");
    }

    @Test
    @DisplayName("프로토콜 상대 URL은 차단")
    void protocolRelativeUrlBlocked() {
        String result = LoginRedirectPolicy.resolve(
                "//example.com"
        );

        assertThat(result).isEqualTo("/vehicles");
    }

    @Test
    @DisplayName("백슬래시를 이용한 우회를 차단")
    void backslashBlocked() {
        String result = LoginRedirectPolicy.resolve(
                "/\\example.com"
        );

        assertThat(result).isEqualTo("/vehicles");
    }

    @Test
    @DisplayName("인코딩된 특수 경로를 차단")
    void encodedPathBlocked() {
        String result = LoginRedirectPolicy.resolve(
                "/%2Fexample.com"
        );

        assertThat(result).isEqualTo("/vehicles");
    }

    @Test
    @DisplayName("로그인 페이지로 반복 이동 불가능")
    void loginLoopBlocked() {
        String result = LoginRedirectPolicy.resolve(
                "/members/login"
        );

        assertThat(result).isEqualTo("/vehicles");
    }

    @Test
    @DisplayName("복귀 경로가 없으면 기본 페이지를 사용")
    void missingRedirectUrl() {
        assertThat(LoginRedirectPolicy.resolve(null))
                .isEqualTo("/vehicles");
    }

    @Test
    @DisplayName("GET 요청은 기존 경로를 유지")
    void getRequestPreservesPath() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/reservations/new"
                );

        String result = LoginRedirectPolicy.fromRequest(request);

        assertThat(result).isEqualTo("/reservations/new");
    }

    @Test
    @DisplayName("예약 POST 요청은 안전한 GET 화면으로 복귀")
    void postReservationRedirectsToForm() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/reservations"
                );

        String result = LoginRedirectPolicy.fromRequest(request);

        assertThat(result).isEqualTo("/reservations/new");
    }

    @Test
    @DisplayName("차량 등록 POST 요청은 등록 화면으로 복귀")
    void postVehicleRedirectsToForm() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/vehicles"
                );

        String result = LoginRedirectPolicy.fromRequest(request);

        assertThat(result).isEqualTo("/vehicles/new");
    }
}