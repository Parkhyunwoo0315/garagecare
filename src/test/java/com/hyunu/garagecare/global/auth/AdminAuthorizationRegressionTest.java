package com.hyunu.garagecare.global.auth;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.domain.MemberRole;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.member.session.SessionConst;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminAuthorizationRegressionTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager entityManager;

    private static final String[] ADMIN_PATHS = {
            "/admin",
            "/admin/reservations",
            "/admin/members",
            "/admin/vehicles",
            "/admin/maintenance-items"
    };

    private static final String[] ADMIN_POST_PATHS = {
            "/admin/reservations/1/confirm",
            "/admin/reservations/1/complete",
            "/admin/maintenance-items",
            "/admin/maintenance-items/1/edit",
            "/admin/maintenance-items/1/activate",
            "/admin/maintenance-items/1/deactivate",
    };

    @Test
    @DisplayName("비로그인 사용자는 관리자 페이지 접근 불가능")
    void guestCannotAccessAdminPages() throws Exception {

        for (String path : ADMIN_PATHS) {
            mockMvc.perform(get(path))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(
                            redirectedUrlPattern(
                                    "/members/login*"
                            )
                    );
        }
    }

    @Test
    @DisplayName("MEMBER는 모든 관리자 페이지에 접근 불가능")
    void memberCannotAccessAdminPages() throws Exception {

        //given
        Member member = createMember(
                "일반 회원",
                "regression-member@test.com"
        );

        MockHttpSession session =
                createSession(member.getId());

        //when & then
        for (String path : ADMIN_PATHS) {
            mockMvc.perform(
                            get(path)
                                    .session(session)
                    )
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("ADMIN은 관리자 페이지에 접근 가능")
    void adminAccessAdminPages() throws Exception {

        //given
        Member member = createAdmin(
                "관리자",
                "regression-admin@test.com"
        );

        MockHttpSession session =
                createSession(member.getId());

        //when & then
        for (String path : ADMIN_PATHS) {
            mockMvc.perform(
                            get(path)
                                    .session(session)
                    )
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID는 관리자 접근 차단")
    void nonexistentMemberCannotAccessAdminPages() throws Exception {

        //given
        MockHttpSession session =
                createSession(Long.MAX_VALUE);

        //when & then
        for (String path : ADMIN_PATHS) {
            mockMvc.perform(
                            get(path)
                                    .session(session)
                    )
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("ADMIN 권한이 MEMBER로 변경되면 접근 차단")
    void downgradedAdminCannotAccessAdminPages() throws Exception {

        //given
        Member admin = createAdmin(
                "권한 변경 관리자",
                "regression-downgrade@test.com"
        );

        MockHttpSession session =
                createSession(admin.getId());

        mockMvc.perform(
                        get("/admin")
                                .session(session)
                )
                .andExpect(status().isOk());

        changeRole(
                admin.getId(),
                MemberRole.MEMBER
        );

        //when & then
        for (String path : ADMIN_PATHS) {
            mockMvc.perform(
                            get(path)
                                    .session(session)
                    )
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("MEMBER 권한이 ADMIN으로 변경되면 관리자 접근 허용")
    void upgradedMemberCannotAccessAdminPages() throws Exception {

        //given
        Member member = createMember(
                "권한 승격 회원",
                "regression-upgrade@test.com"
        );

        MockHttpSession session =
                createSession(member.getId());

        mockMvc.perform(
                        get("/admin")
                                .session(session)
                )
                .andExpect(status().isForbidden());

        changeRole(
                member.getId(),
                MemberRole.ADMIN
        );
        //when & then
        for (String path : ADMIN_PATHS) {
            mockMvc.perform(
                            get(path)
                                    .session(session)
                    )
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("비로그인 사용자는 관리자 POST 요청을 실행 불가능")
    void guestCannotExecuteAdminPostRequests() throws Exception {

        for (String path : ADMIN_POST_PATHS) {
            mockMvc.perform(post(path))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(
                            redirectedUrlPattern("/members/login*")
                    );
        }
    }

    @Test
    @DisplayName("MEMBER는 관리자 POST 요청 실행 불가능")
    void memberCannotExecuteAdminPostRequests() throws Exception {

        //given
        Member member = createMember(
                "일반 회원",
                "regression-post-member@test.com"
        );

        MockHttpSession session =
                createSession(member.getId());

        //when & then
        for (String path : ADMIN_POST_PATHS) {
            mockMvc.perform(
                            post(path)
                                    .session(session)
                    )
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("ADMIN 권한 강등 후 관리자 POST 요청이 차단된다")
    void downgradedAdminCannotExecutePostRequests() throws Exception {

        // given
        Member admin = createAdmin(
                "권한 강등 관리자",
                "regression-post-downgrade@test.com"
        );

        MockHttpSession session =
                createSession(admin.getId());

        mockMvc.perform(
                        get("/admin")
                                .session(session)
                )
                .andExpect(status().isOk());

        changeRole(
                admin.getId(),
                MemberRole.MEMBER
        );

        // when & then
        for (String path : ADMIN_POST_PATHS) {
            mockMvc.perform(
                            post(path)
                                    .session(session)
                    )
                    .andExpect(status().isForbidden());
        }
    }

    private Member createMember(
            String name,
            String email
    ) {
        return memberRepository.save(
                Member.create(
                        name,
                        email,
                        "password"
                )
        );
    }

    private Member createAdmin(
            String name,
            String email
    ) {
        Member member = createMember(name, email);

        changeRole(
                member.getId(),
                MemberRole.ADMIN
        );

        Member admin = memberRepository
                .findById(member.getId())
                .orElseThrow();

        assertThat(admin.getRole())
                .isEqualTo(MemberRole.ADMIN);

        return admin;
    }

    private void changeRole(
            Long memberId,
            MemberRole role
    ) {
        entityManager.flush();

        entityManager.createNativeQuery(
                "UPDATE members SET role = :role WHERE id = :id"
        )
                .setParameter("role", role.name())
                .setParameter("id", memberId)
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();
    }

    private MockHttpSession createSession(Long memberId) {

        MockHttpSession session = new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                memberId
        );

        return session;
    }
}
