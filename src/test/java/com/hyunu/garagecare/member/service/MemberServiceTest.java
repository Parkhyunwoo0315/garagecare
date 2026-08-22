package com.hyunu.garagecare.member.service;

import com.hyunu.garagecare.member.dto.MemberLoginRequest;
import com.hyunu.garagecare.member.dto.MemberSignUpRequest;
import com.hyunu.garagecare.member.exception.DuplicateMemberException;
import com.hyunu.garagecare.member.exception.LoginFailedException;
import com.hyunu.garagecare.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberservice;

    @Autowired
    MemberRepository memberrepository;

    @Test
    @DisplayName("회원가입 성공")
    void signup() {

        // given
        MemberSignUpRequest request =
                new MemberSignUpRequest();
        request.setName("홍길동");
        request.setEmail("test@test.com");
        request.setPassword("12345678");

        // when
        Long memberId =
                memberservice.signUp(request);

        // then
        assertThat(memberId).isNotNull();
        assertThat(
                memberrepository.existsByEmail("test@test.com")
        ).isTrue();
    }

    @Test
    @DisplayName("중복 이메일 회원가입 실패")
    void duplicateEmail() {

        // given
        MemberSignUpRequest request1 =
                new MemberSignUpRequest();

        request1.setName("박현우");
        request1.setEmail("test@test.com");
        request1.setPassword("12345678");

        memberservice.signUp(request1);

        MemberSignUpRequest request2 =
                new MemberSignUpRequest();

        request2.setName("김철수");
        request2.setEmail("test@test.com");
        request2.setPassword("87654321");

        // when & then
        assertThatThrownBy(() ->
                memberservice.signUp(request2)
        ).isInstanceOf(DuplicateMemberException.class);
    }

    @Test
    @DisplayName("로그인 성공")
    void login() {

        //given
        MemberSignUpRequest signUpRequest = new MemberSignUpRequest();

        signUpRequest.setName("박현우");
        signUpRequest.setEmail("test@test.com");
        signUpRequest.setPassword("12345678");

        Long savedMemberId = memberservice.signUp(signUpRequest);

        MemberLoginRequest loginRequest = new MemberLoginRequest();

        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("12345678");

        //when
        Long loginMemberId = memberservice.login(loginRequest);

        //then
        assertThat(loginMemberId).isEqualTo(savedMemberId);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 로그인 실패")
    void loginFailByPassword() {

        //given
        MemberSignUpRequest signUpRequest = new MemberSignUpRequest();

        signUpRequest.setName("박현우");
        signUpRequest.setEmail("test@test.com");
        signUpRequest.setPassword("12345678");

        memberservice.signUp(signUpRequest);

        MemberLoginRequest loginRequest = new MemberLoginRequest();

        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("87654321");

        //when & then
        assertThatThrownBy(
                () -> memberservice.login(loginRequest)
        )
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 이메일이면 로그인 실패")
    void loginFailByEmail() {

        //given
        MemberLoginRequest request = new MemberLoginRequest();

        request.setEmail("unknown@test.com");
        request.setPassword("12345678");

        //when & then
        assertThatThrownBy(
                () -> memberservice.login(request)
        )
                .isInstanceOf(LoginFailedException.class);
    }
}