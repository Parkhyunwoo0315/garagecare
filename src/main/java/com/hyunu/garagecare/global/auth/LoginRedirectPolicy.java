package com.hyunu.garagecare.global.auth;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public final class LoginRedirectPolicy {

    private static final String DEFAULT_URL = "/mypage";

    private LoginRedirectPolicy() {
    }

    public static String resolve(String redirectURL) {

        if (redirectURL == null || redirectURL.isBlank()) {
            return DEFAULT_URL;
        }

        if (!redirectURL.startsWith("/")
                || redirectURL.startsWith("//")
                || redirectURL.contains("\\")) {
            return DEFAULT_URL;
        }

        if (redirectURL.chars().anyMatch(
                Character::isISOControl)) {
            return DEFAULT_URL;
        }

        try {
            URI uri = new URI(redirectURL);

            String path = uri.getRawPath();

            if (uri.isAbsolute()
                    || uri.getRawAuthority() != null
                    || uri.getRawFragment() != null
                    || path == null
                    || !path.startsWith("/")
                    || path.startsWith("//")) {
                return DEFAULT_URL;
            }

            // 인코딩된 특수 경로 우회 방지
            if (path.contains("%")) {
                return DEFAULT_URL;
            }

            // 경로 정규화로 변경되는 URL은 거부
            if (!path.equals(uri.normalize().getRawPath())) {
                return DEFAULT_URL;
            }

            String lowerPath = path.toLowerCase(Locale.ROOT);

            if (lowerPath.equals("/members/login")
                    || lowerPath.equals("/members/logout")) {
                return DEFAULT_URL;
            }

            return redirectURL;

        } catch (URISyntaxException ex) {
            return DEFAULT_URL;
        }
    }

    public static String fromRequest(
            HttpServletRequest request) {

        String method = request.getMethod();
        String path = request.getRequestURI();

        if ("GET".equalsIgnoreCase(method)) {
            String query = request.getQueryString();

            String destination = path +
                    (query == null ? "" : "?" + query);

            return resolve(destination);
        }

        // POST 요청을 로그인 후 자동으로 재실행하지 않음
        if ("/reservations".equals(path)) {
            return "/reservations/new";
        }

        if ("/vehicles".equals(path)) {
            return "/vehicles/new";
        }

        return DEFAULT_URL;
    }
}