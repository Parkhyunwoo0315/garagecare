package com.hyunu.garagecare.member.repository;

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
        Assertion.assertThat(savedMember.getId()).isNotNull();
        Assertion.assertThat(savedMember.getEmail()).isEqualTo("Test@Test.com");
        Assertion.assertThat(savedMember.getRole()).isEqualTo(MemberRole.MEMBER);
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
                memberRepository.existsByEmail("test@test.com")
        //then
        Assertion.assertThat(result).isTrue();
    }
}