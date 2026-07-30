package com.hyunu.garagecare.service.MemberService;

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