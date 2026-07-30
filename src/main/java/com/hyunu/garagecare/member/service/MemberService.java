package com.hyunu.garagecare.member.service;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.dto.MemberSignUpRequest;
import com.hyunu.garagecare.member.exception.DuplicateMemberException;
import com.hyunu.garagecare.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
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

    private void validateDuplicateEmail(String email) {
        if(memberRepository.existsByEmail(email)) {
            throw new DuplicateMemberException();
        }
    }
}