package com.hyunu.garagecare.home.controller;

import com.hyunu.garagecare.member.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

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

        return "home";
    }
}