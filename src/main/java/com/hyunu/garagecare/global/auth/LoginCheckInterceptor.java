package com.hyunu.garagecare.global.auth;

import com.hyunu.garagecare.member.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER_ID) == null) {

            String requestURI = getRequestURI(request);

            String encodedRequestURI = URLEncoder.encode(
                    requestURI,
                    StandardCharsets.UTF_8
            );

            response.sendRedirect(
                    "/members/login?redirectURL=" + encodedRequestURI
            );
            return false;
        }
        return true;
    }

    private String getRequestURI(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURI;
        }
        return requestURI + "?" + queryString;
    }
}
