package com.hyunu.garagecare.admin.dashboard.controller;

import com.hyunu.garagecare.admin.dashboard.dto.AdminDashboardResponse;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminDashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    @DisplayName("ADMIN은 대시보드에 접근 가능")
    void adminCanAccessDashboard() throws Exception {

        //given
        Member admin = createAdmin(
                "대시보드 관리자",
                "admin-dashboard@test.com"
        );

        //when & then
        mockMvc.perform(
                        get("/admin")
                                .session(createSession(admin.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("dashboard"))
                .andExpect(result -> {
                    Object value = result.getModelAndView()
                            .getModel()
                            .get("dashboard");

                    assertThat(value)
                            .isInstanceOf(AdminDashboardResponse.class);

                    AdminDashboardResponse dashboard =
                            (AdminDashboardResponse) value;

                    assertThat(dashboard.memberCount())
                            .isGreaterThanOrEqualTo(1L);
                });
    }

    @Test
    @DisplayName("MEMBER는 대시보드에 접근 불가능")
    void memberCannotAccessDashboard() throws Exception {

        //given
        Member member = memberRepository.save(
                Member.create(
                        "일반 회원",
                        "member-dashboard@test.com",
                        "password"
                )
        );

        //when & then
        mockMvc.perform(
                        get("/admin")
                                .session(createSession(member.getId()))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("비로그인 사용자는 로그인 화면으로 리다이렉트")
    void guestCannotAccessDashboard() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl(
                                "/members/login?redirectURL=%2Fadmin"
                        )
                );
    }

    private Member createAdmin(String name, String email) {
        Member member = memberRepository.save(
                Member.create(name, email, "password")
        );

        entityManager.flush();

        entityManager.createNativeQuery(
                "UPDATE members SET role = 'ADMIN' WHERE id = :id"
                )
                .setParameter("id", member.getId())
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        Member admin = memberRepository.findById(member.getId())
                .orElseThrow();

        assertThat(admin.getRole())
                .isEqualTo(MemberRole.ADMIN);

        return admin;
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
