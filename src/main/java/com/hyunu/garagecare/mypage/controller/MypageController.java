
package com.hyunu.garagecare.mypage.controller;

import com.hyunu.garagecare.member.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MypageController {

    @GetMapping("/mypage")
    public String mypage(
            HttpServletRequest request,
            Model model
    ) {
        HttpSession session = request.getSession(false);

        if (session == null ||
                session.getAttribute(
                        SessionConst.LOGIN_MEMBER_ID) == null) {

            return "redirect:/members/login"
                    + "?redirectURL=/mypage";
        }

        model.addAttribute("loggedIn", true);

        return "mypage/index";
    }
}
