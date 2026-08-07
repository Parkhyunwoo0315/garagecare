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
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/signup")
    public String signUp(
            @Valid @ModelAttribute("form")
            MemberSignUpRequest request,
            BindingResult bindingResult
    ) {
        if(bindingResult.hasErrors()) {
            return "member/signup-form";
        }

        memberService.signUp(request);
        return "redirect:/members/signup/complete";
    }

    @GetMapping("/signup/complete")
    public String signUpComplete() {
        return "member/signup-complete";
    }

    // =========================
    // Login
    // =========================

    @GetMapping("/login")
    public String loginForm(
            @RequestParam(
                    name = "redirectURL",
                    required = false
            ) String redirectURL,
            Model model
    ) {
        model.addAttribute("form", new MemberLoginRequest());
        model.addAttribute("redirectURL", redirectURL);

        return "member/login-form";
    }

    @PostMapping("/login")
    public String login(
            @Valid
            @ModelAttribute("form")
            MemberLoginRequest request,
            BindingResult bindingResult,
            @RequestParam(
                    name = "redirectURL",
                    required = false
            ) String  redirectURL,
            HttpServletRequest httpRequest
            ) {
        if(bindingResult.hasErrors()) {
            return "member/login-form";
        }
        try {
            Long memberId = memberService.login(request);
            HttpSession session = httpRequest.getSession();
            session.setAttribute(
                    SessionConst.LOGIN_MEMBER_ID,
                    memberId
            );
            return "redirect:" + resolveRedirectURL(redirectURL);

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

    private String resolveRedirectURL(
            String redirectURL
    ) {
        if (redirectURL == null || redirectURL.isBlank()) {
            return "/";
        }
        if (redirectURL.startsWith("/") || redirectURL.startsWith("//")) {
            return "/";
        }
        return redirectURL;
    }
}