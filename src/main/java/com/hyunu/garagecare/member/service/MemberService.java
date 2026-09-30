package com.hyunu.garagecare.member.service;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.domain.MemberRole;
import com.hyunu.garagecare.member.dto.MemberLoginRequest;
import com.hyunu.garagecare.member.dto.MemberSignUpRequest;
import com.hyunu.garagecare.member.dto.admin.AdminMemberDetailResponse;
import com.hyunu.garagecare.member.dto.admin.AdminMemberListResponse;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import com.hyunu.garagecare.member.exception.DuplicateMemberException;
import com.hyunu.garagecare.member.exception.LoginFailedException;
import com.hyunu.garagecare.member.exception.MemberNotFoundException;
import com.hyunu.garagecare.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private static final int ADMIN_MEMBER_PAGE_SIZE = 20;

    private final MemberRepository memberRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signUp(MemberSignUpRequest request) {
        validateDuplicateEmail(request.getEmail());

        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        Member member = Member.create(
                request.getName(),
                request.getEmail(),
                encodedPassword
        );

        Member savedMember = memberRepository.save(member);

        return savedMember.getId();
    }

    public Long login(MemberLoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(LoginFailedException::new);

        if (!passwordEncoder.matches(
                request.getPassword(),
                member.getPassword()
        )) {
            throw new LoginFailedException();
        }
        return member.getId();
    }

    public boolean isAdmin(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        return member.getRole() == MemberRole.ADMIN;
    }

    public Page<AdminMemberListResponse> getAdminMembers(
            int page
    ) {
        Pageable pageable = PageRequest.of(
                page,
                ADMIN_MEMBER_PAGE_SIZE,
                Sort.by(
                        Sort.Order.desc("id")
                )
        );

        return memberRepository
                .findAll(pageable)
                .map(AdminMemberListResponse::from);
    }

    public AdminMemberDetailResponse getAdminMemberDetail(
            Long memberId
    ) {
        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        List<Vehicle> vehicles =
                vehicleRepository.findAllByMemberId(memberId);

        return AdminMemberDetailResponse.of(
                member,
                vehicles
        );
    }

    private void validateDuplicateEmail(String email) {
        if(memberRepository.existsByEmail(email)) {
            throw new DuplicateMemberException();
        }
    }
}