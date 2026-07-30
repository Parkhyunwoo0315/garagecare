package com.hyunu.garagecare.member.service;

@springBootTest
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
                memberService.signUp(request);

        // then
        Assertion.assertThat(memberId).isNotNull();
        Assertion.assertThat(
                memberRepository.existsByEmail("test@test.com")
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

        memberService.signUp(request1);

        MemberSignUpRequest request2 =
                new MemberSignUpRequest();

        request2.setName("김철수");
        request2.setEmail("test@test.com");
        request2.setPassword("87654321");

        // when & then
        assertThatThrownBy(() ->
                memberService.signUp(request2)
        ).isInstanceOf(DuplicateMemberException.class);

    }
}