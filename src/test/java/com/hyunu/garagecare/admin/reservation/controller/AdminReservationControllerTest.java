package com.hyunu.garagecare.admin.reservation.controller;

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
class AdminReservationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    @DisplayName("ADMIN은 관리자 예약 목록 화면에 접근 가능")
    void adminCanAccessReservationList() throws Exception {

        // given
        Member admin = createAdmin(
                "관리자",
                "admin-reservation@test.com"
        );

        MockHttpSession session = new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                admin.getId()
        );

        // when & then
        mockMvc.perform(
                        get("/admin/reservations")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        view().name("admin/reservation/list")
                )
                .andExpect(
                        model().attributeExists("reservations")
                );
    }

    @Test
    @DisplayName("MEMBER는 관리자 예약 목록에 접근 불가능")
    void memberCannotAccessReservationList() throws Exception {

        // given
        Member member = memberRepository.save(
                Member.create(
                        "일반회원",
                        "member-reservation@test.com",
                        "password"
                )
        );

        MockHttpSession session = new MockHttpSession();

        session.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                member.getId()
        );

        // when & then
        mockMvc.perform(
                        get("/admin/reservations")
                                .session(session)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("비로그인 사용자는 관리자 예약 목록 접근 시 로그인 화면으로 이동")
    void unauthenticatedUserCannotAccessReservationList() throws Exception {

        mockMvc.perform(
                        get("/admin/reservations")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrlPattern(
                                "/members/login?redirectURL=*"
                        )
                );
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
                .setParameter("id", member.getId())
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        return memberRepository
                .findById(member.getId())
                .orElseThrow();
    }
}
