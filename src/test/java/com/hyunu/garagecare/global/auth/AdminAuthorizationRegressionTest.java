package com.hyunu.garagecare.global.auth;

import com.hyunu.garagecare.maintenance.domain.MaintenanceItem;
import com.hyunu.garagecare.maintenance.repository.MaintenanceItemRepository;
import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.domain.MemberRole;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.member.session.SessionConst;
import com.hyunu.garagecare.reservation.domain.Reservation;
import com.hyunu.garagecare.reservation.domain.ReservationStatus;
import com.hyunu.garagecare.reservation.repository.ReservationRepository;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

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

    @Autowired
    MaintenanceItemRepository maintenanceItemRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    VehicleRepository vehicleRepository;

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
    void upgradedMemberCanAccessAdminPages() throws Exception {

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

    @Test
    @DisplayName("MEMBER의 정비 항목 비활성화 요청은 차단되고 DB 상태는 유지")
    void memberCannotDeactivateMaintenanceItems() throws Exception {

        //given
        Member member = createMember(
                "정비 항목 테스트 회원",
                "regression-maintenance-deactivate@test.com"
        );

        MockHttpSession session =
                createSession(member.getId());

        MaintenanceItem item = maintenanceItemRepository.save(
                MaintenanceItem.create(
                        "회귀 테스트 엔진오일 교환",
                        "권한 검증용 정비 항목",
                        80000L
                )
        );

        Long itemId = item.getId();

        entityManager.flush();
        entityManager.clear();

        //when
        mockMvc.perform(
                        post("/admin/maintenance-items/{id}/deactivate", itemId)
                                .session(session)
                )
                .andExpect(status().isForbidden());

        //then
        entityManager.clear();

        MaintenanceItem persistedItem = maintenanceItemRepository
                .findById(itemId)
                .orElseThrow();

        assertThat(persistedItem.isActive()).isTrue();
    }

    @Test
    @DisplayName("MEMBER의 정비 항목 활성화 요청은 차단되고 DB 상태 유지")
    void memberCannotActivateMaintenanceItem() throws Exception {

        //given
        Member member = createMember(
                "정비 항목 활성화 테스트 회원",
                "regression-maintenance-activate@test.com"
        );

        MockHttpSession session =
                createSession(member.getId());

        MaintenanceItem item = MaintenanceItem.create(
                "회귀 테스트 브레이크 패드 교환",
                "권한 검증은 비활성 정비 항목",
                120000L
        );

        item.deactivate();

        maintenanceItemRepository.save(item);

        Long itemId = item.getId();

        entityManager.flush();
        entityManager.clear();

        //when
        mockMvc.perform(
                        post("/admin/maintenance-items/{id}/activate", itemId)
                                .session(session)
                )
                .andExpect(status().isForbidden());

        //then
        entityManager.clear();

        MaintenanceItem persistedItem = maintenanceItemRepository
                .findById(itemId)
                .orElseThrow();

        assertThat(persistedItem.isActive()).isFalse();
    }

    private Reservation createReservation(
            Member member,
            String vehicleNumber
    ) {
        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        member,
                        vehicleNumber,
                        "BMW",
                        "Z4 M40i(G29)",
                        2024
                )
        );

        return reservationRepository.save(
                Reservation.create(
                        member,
                        vehicle,
                        LocalDate.now().plusDays(7),
                        LocalTime.of(10,0)
                )
        );
    }

    @Test
    @DisplayName("MEMBER의 예약 확정 요청은 차단되고 PENDING 상태 유지")
    void memberCannotConfirmReservation() throws Exception {

        //given
        Member member = createMember(
                "예약 확정 테스트 회원",
                "regression-confirm@test.com"
        );

        MockHttpSession session =
                createSession(member.getId());

        Reservation reservation = createReservation(
                member,
                "11하1111"
        );

        Long reservationId = reservation.getId();

        entityManager.flush();
        entityManager.clear();

        //when
        mockMvc.perform(
                        post("/admin/reservations/{id}/confirm", reservationId)
                                .session(session)
                )
                .andExpect(status().isForbidden());

        //then
        entityManager.flush();
        entityManager.clear();

        Reservation persistedReservation = reservationRepository
                .findById(reservationId)
                .orElseThrow();

        assertThat(persistedReservation.getStatus())
                .isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("MEMBER의 정비 완료 요청은 차단되고 CONFIRMED 상태 유지")
    void memberCannotCompleteReservation() throws Exception {

        //given
        Member member = createMember(
                "정비 완료 테스트 회원",
                "regression-complete@test.com"
        );

        MockHttpSession session =
                createSession(member.getId());

        Reservation reservation = createReservation(
                member,
                "22카2222"
        );

        reservation.confirm();

        Long reservationId = reservation.getId();

        entityManager.flush();
        entityManager.clear();

        //when
        mockMvc.perform(
                        post("/admin/reservations/{id}/complete", reservationId)
                                .session(session)
                )
                .andExpect(status().isForbidden());

        //then
        entityManager.flush();
        entityManager.clear();

        Reservation persistedReservation = reservationRepository
                .findById(reservationId)
                .orElseThrow();

        assertThat(persistedReservation.getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("비로그인 사용자의 예약 확정 요청은 차단되고 PENDING 상태 유지")
    void guestCannotConfirmReservation() throws Exception {

        //given
        Member member = createMember(
                "비로그인 테스트 회원",
                "regression-guest-confirm@test.com"
        );

        Reservation reservation = createReservation(
                member,
                "33차3333"
        );

        Long reservationId = reservation.getId();

        entityManager.flush();
        entityManager.clear();

        //when
        mockMvc.perform(
                        post("/admin/reservations/{id}/confirm", reservationId)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrlPattern("/members/login*")
                );

        //then
        entityManager.flush();
        entityManager.clear();

        Reservation persistedReservation = reservationRepository
                .findById(reservationId)
                .orElseThrow();

        assertThat(persistedReservation.getStatus())
                .isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("ADMIN 권한 강등 후 예약 확정 요청은 차단되고 PENDING 상태 유지")
    void downgradedAdminCannotConfirmReservation() throws Exception {

        //given
        Member admin = createAdmin(
                "예약 권한 강등 관리자",
                "regression-downgrade-confirm@test.com"
        );

        MockHttpSession session =
                createSession(admin.getId());

        Reservation reservation = createReservation(
                admin,
                "44디4444"
        );

        Long reservationId = reservation.getId();

        entityManager.flush();
        entityManager.clear();

        //관리자 접근 가능 여부 확인
        mockMvc.perform(
                        get("/admin")
                                .session(session)
                )
                .andExpect(status().isOk());

        //관리자 접근 권한 강등
        changeRole(
                admin.getId(),
                MemberRole.MEMBER
        );

        //when
        mockMvc.perform(
                        post("/admin/reservations/{id}/confirm", reservationId)
                                .session(session)
                )
                .andExpect(status().isForbidden());

        //then
        entityManager.flush();
        entityManager.clear();

        Reservation persistedReservation = reservationRepository
                .findById(reservationId)
                .orElseThrow();

        assertThat(persistedReservation.getStatus())
                .isEqualTo(ReservationStatus.PENDING);
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
