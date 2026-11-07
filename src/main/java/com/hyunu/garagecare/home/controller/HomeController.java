package com.hyunu.garagecare.home.controller;

import com.hyunu.garagecare.member.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Value;

@Controller
public class HomeController {

    @Value("${garagecare.maps.naver.client-id:}")
    private String naverMapsClientId;

    @GetMapping("/")
    public String home(
            HttpServletRequest request,
            Model model
    ) {
        HttpSession session = request.getSession(false);

        boolean loggedIn = session != null
                && session.getAttribute(
                SessionConst.LOGIN_MEMBER_ID
        ) != null;

        model.addAttribute("loggedIn", loggedIn);

        model.addAttribute("naverMapsClientId", naverMapsClientId);
        model.addAttribute(
                "naverMapEnabled",
                !naverMapsClientId.isBlank()
        );

        return "home";
    }
}