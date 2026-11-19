
package com.hyunu.garagecare.global.web;

import com.hyunu.garagecare.member.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class HeaderLoginModelAdvice {

    @ModelAttribute("loggedIn")
    public boolean loggedIn(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return false;
        }

        Object memberId = session.getAttribute(
                SessionConst.LOGIN_MEMBER_ID
        );

        return memberId instanceof Long;
    }
}
