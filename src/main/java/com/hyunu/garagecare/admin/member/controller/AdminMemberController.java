package com.hyunu.garagecare.admin.member.controller;

import com.hyunu.garagecare.member.dto.admin.AdminMemberListResponse;
import com.hyunu.garagecare.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/members")
public class AdminMemberController {

    private final MemberService memberService;

    @GetMapping
    public String members(
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<AdminMemberListResponse> members =
                memberService.getAdminMembers(page);

        model.addAttribute(
                "members",
                members
        );

        return "admin/member/list";
    }

    @GetMapping("/{memberId}")
    public String memberDetail(
            @PathVariable Long memberId,
            Model model
    ) {
        model.addAttribute(
                "member",
                memberService.getAdminMemberDetail(memberId)
        );

        return "admin/member/detail";
    }
}
