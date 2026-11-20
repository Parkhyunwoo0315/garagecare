package com.hyunu.garagecare.global.auth;

import com.hyunu.garagecare.member.domain.MemberRole;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.member.session.SessionConst;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class AdminNavigationModelAdvice {

    private final MemberRepository memberRepository;

    @ModelAttribute("adminUser")
    public boolean isAdminUser(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return false;
        }

        Object loginMemberId = session.getAttribute(
                SessionConst.LOGIN_MEMBER_ID
        );

        if (!(loginMemberId instanceof Long memberId)) {
            return false;
        }

        return memberRepository.findById(memberId)
                .map(member -> member.getRole() == MemberRole.ADMIN)
                .orElse(false);
    }
}