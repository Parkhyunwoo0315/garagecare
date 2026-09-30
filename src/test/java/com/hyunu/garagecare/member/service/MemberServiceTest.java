package com.hyunu.garagecare.member.service;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.dto.admin.AdminMemberDetailResponse;
import com.hyunu.garagecare.member.dto.admin.AdminMemberListResponse;
import com.hyunu.garagecare.member.exception.MemberNotFoundException;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import org.springframework.data.domain.Page;

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

    @Autowired
    VehicleRepository vehicleRepository;

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

    @Test
    @DisplayName("관리자는 전체 회원 목록을 조회 가능")
    void getAdminMembers() {

        // given
        Member member1 = memberrepository.save(
                Member.create(
                        "회원1",
                        "admin-member-list1@test.com",
                        "password"
                )
        );

        Member member2 = memberrepository.save(
                Member.create(
                        "회원2",
                        "admin-member-list2@test.com",
                        "password"
                )
        );

        // when
        Page<AdminMemberListResponse> result =
                memberservice.getAdminMembers(0);

        // then
        assertThat(result.getContent())
                .extracting(AdminMemberListResponse::memberId)
                .contains(
                        member1.getId(),
                        member2.getId()
                );

        assertThat(result.getContent())
                .extracting(AdminMemberListResponse::email)
                .contains(
                        "admin-member-list1@test.com",
                        "admin-member-list2@test.com"
                );
    }

    @Test
    @DisplayName("회원이 없으면 관리자 회원 목록은 빈 페이지를 반환")
    void getEmptyAdminMembers() {

        // when
        Page<AdminMemberListResponse> result =
                memberservice.getAdminMembers(0);

        // then
        assertThat(result).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getNumber()).isZero();
    }

    @Test
    @DisplayName("관리자는 회원 상세 정보를 조회 가능")
    void getAdminMemberDetail() {

        // given
        Member member = memberrepository.save(
                Member.create(
                        "상세 조회 회원",
                        "admin-member-detail@test.com",
                        "password"
                )
        );

        // when
        AdminMemberDetailResponse result =
                memberservice.getAdminMemberDetail(
                        member.getId()
                );

        // then
        assertThat(result.memberId())
                .isEqualTo(member.getId());

        assertThat(result.name())
                .isEqualTo("상세 조회 회원");

        assertThat(result.email())
                .isEqualTo("admin-member-detail@test.com");

        assertThat(result.active())
                .isTrue();

        assertThat(result.vehicles())
                .isEmpty();
    }

    @Test
    @DisplayName("관리자 회원 상세 조회 시 등록 차량을 함께 반환")
    void getAdminMemberDetailWithVehicles() {

        // given
        Member member = memberrepository.save(
                Member.create(
                        "차량 보유 회원",
                        "admin-member-vehicle@test.com",
                        "password"
                )
        );

        Vehicle vehicle1 = vehicleRepository.save(
                Vehicle.create(
                        member,
                        "12가3456",
                        "Porsche",
                        "911 GT3",
                        2022
                )
        );

        Vehicle vehicle2 = vehicleRepository.save(
                Vehicle.create(
                        member,
                        "34나5678",
                        "BMW",
                        "M3",
                        2024
                )
        );

        // when
        AdminMemberDetailResponse result =
                memberservice.getAdminMemberDetail(
                        member.getId()
                );

        // then
        assertThat(result.vehicles())
                .hasSize(2);

        assertThat(result.vehicles())
                .extracting(vehicle -> vehicle.vehicleId())
                .containsExactlyInAnyOrder(
                        vehicle1.getId(),
                        vehicle2.getId()
                );

        assertThat(result.vehicles())
                .extracting(vehicle -> vehicle.vehicleNumber())
                .containsExactlyInAnyOrder(
                        "12가3456",
                        "34나5678"
                );
    }

    @Test
    @DisplayName("존재하지 않는 회원의 관리자 상세 조회는 실패")
    void adminMemberDetailNotFound() {

        // when & then
        assertThatThrownBy(
                () -> memberservice.getAdminMemberDetail(
                        Long.MAX_VALUE
                )
        )
                .isInstanceOf(
                        MemberNotFoundException.class
                );
    }

}