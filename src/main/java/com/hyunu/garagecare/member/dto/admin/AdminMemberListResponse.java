package com.hyunu.garagecare.member.dto.admin;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.domain.MemberRole;

public record AdminMemberListResponse(
        Long memberId,
        String name,
        String email,
        MemberRole role,
        boolean active
) {

    public static AdminMemberListResponse from(Member member) {
        return new AdminMemberListResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getRole(),
                member.isActive()
        );
    }
}
