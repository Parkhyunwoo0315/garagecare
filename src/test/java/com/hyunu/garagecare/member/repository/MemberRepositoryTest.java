package com.hyunu.garagecare.member.repository;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.domain.MemberRole;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("회원 저장")
    void save() {

        //given
        Member member = Member.create(
                "박현우",
                "test@test.com",
                "password"
        );

        //when
        Member savedMember = memberRepository.save(member);

        //then
        assertThat(savedMember.getId()).isNotNull();
        assertThat(savedMember.getEmail()).isEqualTo("test@test.com");
        assertThat(savedMember.getRole()).isEqualTo(MemberRole.MEMBER);
    }

    @Test
    @DisplayName("이메일 존재 여부 조회")
    void existByEmail() {

        //given
        Member member = Member.create(
                "박현우",
                "test@test.com",
                "password"
        );

        memberRepository.save(member);

        //when
        boolean result =
                memberRepository.existsByEmail("test@test.com");
        //then
        assertThat(result).isTrue();
    }
}