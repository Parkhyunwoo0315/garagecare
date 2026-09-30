package com.hyunu.garagecare.admin.member.controller;

import com.hyunu.garagecare.member.domain.Member;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminMemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    @DisplayName("ADMIN은 관리자 회원 목록 화면에 접근 가능")
    void adminCanAccessMemberList() throws Exception {

        // given
        Member admin = createAdmin(
                "회원 관리자",
                "admin-member-controller@test.com"
        );

        MockHttpSession session = createSession(
                admin.getId()
        );

        // when & then
        mockMvc.perform(
                        get("/admin/members")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        view().name("admin/member/list")
                )
                .andExpect(
                        model().attributeExists("members")
                );
    }

    @Test
    @DisplayName("ADMIN은 관리자 회원 상세 화면에 접근 가능")
    void adminCanAccessMemberDetail() throws Exception {

        // given
        Member admin = createAdmin(
                "상세 관리자",
                "admin-member-detail-controller@test.com"
        );

        Member member = memberRepository.save(
                Member.create(
                        "조회 회원",
                        "member-detail-controller@test.com",
                        "password"
                )
        );

        MockHttpSession session = createSession(
                admin.getId()
        );

        // when & then
        mockMvc.perform(
                        get(
                                "/admin/members/{memberId}",
                                member.getId()
                        )
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        view().name("admin/member/detail")
                )
                .andExpect(
                        model().attributeExists("member")
                );
    }

    @Test
    @DisplayName("MEMBER는 관리자 회원 목록에 접근 불가능")
    void memberCannotAccessMemberList() throws Exception {

        // given
        Member member = memberRepository.save(
                Member.create(
                        "일반 회원",
                        "member-admin-access@test.com",
                        "password"
                )
        );

        MockHttpSession session = createSession(
                member.getId()
        );

        // when & then
        mockMvc.perform(
                        get("/admin/members")
                                .session(session)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MEMBER는 관리자 회원 상세 화면에 접근 불가능")
    void memberCannotAccessMemberDetail() throws Exception {

        // given
        Member member = memberRepository.save(
                Member.create(
                        "일반 회원",
                        "member-detail-access@test.com",
                        "password"
                )
        );

        MockHttpSession session = createSession(
                member.getId()
        );

        // when & then
        mockMvc.perform(
                        get(
                                "/admin/members/{memberId}",
                                member.getId()
                        )
                                .session(session)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("비로그인 사용자는 관리자 회원 목록 접근 시 로그인 화면으로 이동")
    void unauthenticatedUserCannotAccessMemberList() throws Exception {

        mockMvc.perform(
                        get("/admin/members")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrlPattern(
                                "/members/login?redirectURL=*"
                        )
                );
    }

    @Test
    @DisplayName("비로그인 사용자는 관리자 회원 상세 접근 시 로그인 화면으로 이동")
    void unauthenticatedUserCannotAccessMemberDetail() throws Exception {

        mockMvc.perform(
                        get("/admin/members/1")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrlPattern(
                                "/members/login?redirectURL=*"
                        )
                );
    }

    private MockHttpSession createSession(
            Long memberId
    ) {
        MockHttpSession session =
                new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                memberId
        );

        return session;
    }

    private Member createAdmin(
            String name,
            String email
    ) {
        Member member = memberRepository.save(
                Member.create(
                        name,
                        email,
                        "password"
                )
        );

        entityManager.createNativeQuery(
                        "update members set role = 'ADMIN' where id = :id"
                )
                .setParameter(
                        "id",
                        member.getId()
                )
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        return memberRepository
                .findById(member.getId())
                .orElseThrow();
    }
}
