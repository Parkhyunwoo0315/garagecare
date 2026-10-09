package com.hyunu.garagecare.admin.maintenance.controller;

import com.hyunu.garagecare.maintenance.domain.MaintenanceItem;
import com.hyunu.garagecare.maintenance.repository.MaintenanceItemRepository;
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

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminMaintenanceItemControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MaintenanceItemRepository maintenanceItemRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    @DisplayName("ADMIN은 관리자 정비 항목 목록에 접근 가능")
    void adminCanAccessMaintenanceItemList() throws Exception {

        //given
        Member admin = createAdmin(
                "정비 관리자,",
                "admin-maintenance-list@test.com"
        );

        MockHttpSession session =
                createSession(admin.getId());

        //when & then
        mockMvc.perform(
                        get("/admin/maintenance-items")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        view().name("admin/maintenance/list")
                )
                .andExpect(
                        model().attributeExists(
                                "maintenanceItems"
                        )
                );
    }

    @Test
    @DisplayName("ADMIN은 정비 항목 등록 화면에 접근 가능")
    void adminCanAccessCreateForm() throws Exception {

        //given
        Member admin = createAdmin(
                "등록 관리자",
                "admin-maintenance-create-form@test.com"
        );

        MockHttpSession session =
                createSession(admin.getId());

        //when & then
        mockMvc.perform(
                        get("/admin/maintenance-items/new")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        view().name("admin/maintenance/form")
                )
                .andExpect(
                        model().attributeExists("form")
                );
    }

    @Test
    @DisplayName("ADMIN은 정비 항목을 등록 가능")
    void adminCanCreateMaintenanceItem() throws Exception {

        //given
        Member admin = createAdmin(
                "등록 관리자",
                "admin-maintenance-create@test.com"
        );

        MockHttpSession session =
                createSession(admin.getId());

        //when
        mockMvc.perform(
                post("/admin/maintenance-items")
                        .session(session)
                        .param(
                                "name",
                                "브레이크 패드 교환"
                        )
                        .param(
                                "description",
                                "브레이크 패드 상태 확인 및 교환"
                        )
                        .param(
                                "estimatedPrice",
                                "180000"
                        )
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl(
                                "/admin/maintenance-items"
                        )
                );

        //then
        assertThat(
                maintenanceItemRepository
                        .findAll()
                        .stream()
                        .anyMatch(item ->
                                item.getName().equals(
                                        "브레이크 패드 교환"
                                )
                        )
        ).isTrue();
    }

    @Test
    @DisplayName("ADMIN은 정비 항목 수정 화면에 접근 가능")
    void adminCanAccessEditForm() throws Exception {

        //given
        Member admin = createAdmin(
                "수정 관리자",
                "admin-maintenance-edit-form@test.com"
        );

        MaintenanceItem item =
                maintenanceItemRepository.save(
                        MaintenanceItem.create(
                                "미션오일 교환",
                                "미션오일 점검",
                                200000L
                        )
                );

        MockHttpSession session =
                createSession(admin.getId());

        //when & then
        mockMvc.perform(
                    get(
                            "/admin/maintenance-items/{id}/edit",
                            item.getId()
                    )
                            .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        view().name("admin/maintenance/form")
                )
                .andExpect(
                        model().attributeExists("form")
                );
    }

    @Test
    @DisplayName("ADMIN은 정비 항목을 수정 가능")
    void adminCanUpdateMaintenanceItem() throws Exception {

        //given
        Member admin = createAdmin(
                "수정 관리자",
                "admin-maintenance-update@test.com"
        );

        MaintenanceItem item =
                maintenanceItemRepository.save(
                        MaintenanceItem.create(
                                "냉각수",
                                "기존 설명",
                                50000L
                        )
                );

        MockHttpSession session =
                createSession(admin.getId());

        //when
        mockMvc.perform(
                        post(
                                "/admin/maintenance-items/{id}/edit",
                                item.getId()
                        )
                                .session(session)
                                .param(
                                        "name",
                                        "냉각수 교환"
                                )
                                .param(
                                        "description",
                                        "냉각수 점검 및 교환"
                                )
                                .param(
                                        "estimatedPrice",
                                        "70000"
                                )
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl(
                                "/admin/maintenance-items"
                        )
                );

        //then
        entityManager.flush();
        entityManager.clear();

        MaintenanceItem updatedItem =
                maintenanceItemRepository
                        .findById(item.getId())
                        .orElseThrow();

        assertThat(updatedItem.getName())
                .isEqualTo("냉각수 교환");

        assertThat(updatedItem.getEstimatedPrice())
                .isEqualTo(70000L);
    }

    @Test
    @DisplayName("ADMIN은 정비 항목을 비활성화 가능")
    void adminCanDeactivateMaintenanceItem() throws Exception {

        //given
        Member admin = createAdmin(
                "상태 관리자",
                "admin-maintenance-deactivate@test.com"
        );

        MaintenanceItem item =
                maintenanceItemRepository.save(
                        MaintenanceItem.create(
                                "와이퍼 교체",
                                "와이퍼 블레이드 교체",
                                30000L
                        )
                );

        MockHttpSession session =
                createSession(admin.getId());

        //when
        mockMvc.perform(
                        post(
                                "/admin/maintenance-items/{id}/deactivate",
                                item.getId()
                        )
                                .session(session)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl(
                                "/admin/maintenance-items"
                        )
                );

        //then
        assertThat(item.isActive())
                .isFalse();
    }

    @Test
    @DisplayName("ADMIN은 정비 항목을 다시 활성화 가능")
    void adminCanActivateMaintenanceItem() throws Exception {

        //given
        Member admin = createAdmin(
                "상태 관리자",
                "admin-maintenance-activate@test.com"
        );

        MaintenanceItem item =
                MaintenanceItem.create(
                        "에어컨 가스 충전",
                        "에어컨 냉매 점검 및 충전",
                        100000L
                );

        item.deactivate();

        maintenanceItemRepository.save(item);

        MockHttpSession session =
                createSession(admin.getId());

        //when
        mockMvc.perform(
                        post(
                                "/admin/maintenance-items/{id}/activate",
                                item.getId()
                        )
                                .session(session)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl(
                                "/admin/maintenance-items"
                        )
                );

        //then
        assertThat(item.isActive())
                .isTrue();
    }

    @Test
    @DisplayName("MEMBER는 관리자 정비 항목 목록에 접근 불가능")
    void memberCannotAccessMaintenanceItemList() throws Exception {

        //given
        Member member = memberRepository.save(
                Member.create(
                        "일반 회원",
                        "member-maintenance-access@test.com",
                        "password"
                )
        );

        MockHttpSession session =
                createSession(member.getId());

        //when & then
        mockMvc.perform(
                        get("/admin/maintenance-items")
                                .session(session)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MEMBER는 관리자 정비 항목 상태를 변경 불가능")
    void memberCannotChangeMaintenanceItemStatus() throws Exception {

        //given
        Member member = memberRepository.save(
                Member.create(
                        "일반 회원",
                        "member-maintenance-status@test.com",
                        "password"
                )
        );

        MaintenanceItem item =
                maintenanceItemRepository.save(
                        MaintenanceItem.create(
                                "타이어 공기압 확인",
                                "타이어 공기압 충전",
                                20000L
                        )
                );

        MockHttpSession session =
                createSession(member.getId());

        //when & then
        mockMvc.perform(
                        post(
                                "/admin/maintenance-items/{id}/deactivate",
                                item.getId()
                        )
                                .session(session)
                )
                .andExpect(status().isForbidden());

        assertThat(item.isActive())
                .isTrue();
    }

    @Test
    @DisplayName("비로그인 사용자는 관리자 정비 항목 접근 시 로그인 화면으로 이동")
    void unauthenticatedUserCannotAccessMaintenanceItems() throws Exception {

        mockMvc.perform(
                        get("/admin/maintenance-items")
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
    ){
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
    ){
        Member member = memberRepository.save(
                Member.create(
                        name,
                        email,
                        "password"
                )
        );

        entityManager.createNativeQuery(
                "update members " +
                        "set role = 'ADMIN' " +
                        "where id = :id"
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
