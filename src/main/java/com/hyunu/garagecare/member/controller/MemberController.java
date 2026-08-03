package com.hyunu.garagecare.member.controller;

import com.hyunu.garagecare.member.dto.MemberLoginRequest;
import com.hyunu.garagecare.member.exception.LoginFailedException;
import com.hyunu.garagecare.member.service.MemberService;
import com.hyunu.garagecare.member.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import com.hyunu.garagecare.member.dto.MemberSignUpRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    // =========================
    // Sign Up
    // =========================

    @GetMapping("/signup")
    public String signUpForm(Model model) {
        model.addAttribute("form", new MemberSignUpRequest());
        return "member/signup-form";
    }

    @PostMapping("signup")
    public String signUp(
            @Valid @ModelAttribute("form") MemberSignUpRequest request,
            BindingResult bindingResult
    ) {
        if(bindingResult.hasErrors()) {
            return "member/sign-form";
        }

        memberService.signUp(request);
        return "redirect:/member/signup/complete";
    }

    @GetMapping("/sign/complate")
    public String signUpComplate() {
        return "member/signup-complate";
    }

    // =========================
    // Login
    // =========================

    @GetMapping("/login")
    public String login(
            @Valid @ModelAttribute("form")MemberLoginRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest
            ) {
        if(bindingResult.hasErrors()) {
            return "member/login-form";
        }
        try {
            Long memberId = memberService.login(request);
            HttpSession sesssion = httpRequest.getSession();
            sesssion.setAttribute(
                    SessionConst.LOGIN_MEMBER_ID,
                    memberId
            );
            return "redirect:/";

        } catch (LoginFailedException exception) {
            bindingResult.reject(
                    "loginFailed",
                    exception.getMessage()
            );
            return "member/login-form";

        }
    }

    // =========================
    // Logout
    // =========================

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }
}