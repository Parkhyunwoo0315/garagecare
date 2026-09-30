package com.hyunu.garagecare.member.dto.admin;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.domain.MemberRole;
import com.hyunu.garagecare.vehicle.domain.Vehicle;

import java.util.List;

public record AdminMemberDetailResponse(
        Long memberId,
        String name,
        String email,
        MemberRole role,
        boolean active,
        List<AdminMemberVehicleResponse> vehicles
) {

    public static AdminMemberDetailResponse of(
            Member member,
            List<Vehicle> vehicles
    ) {
        return new AdminMemberDetailResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getRole(),
                member.isActive(),
                vehicles.stream()
                        .map(AdminMemberVehicleResponse::from)
                        .toList()
        );
    }
}
