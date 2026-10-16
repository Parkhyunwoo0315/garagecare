package com.hyunu.garagecare.global.auth;

import com.hyunu.garagecare.maintenance.domain.MaintenanceItem;
import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.member.session.SessionConst;
import com.hyunu.garagecare.reservation.domain.Reservation;
import com.hyunu.garagecare.reservation.domain.ReservationStatus;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminMutationAuthorizationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager entityManager;

    private MockHttpSession memberSession;
    private Member member;

    @BeforeEach
    void setUp() {

        member = memberRepository.save(
                Member.create(
                        "권한 검증 회원",
                        "mutation-auth-member@test.com",
                        "password"
                )
        );

        memberSession = new MockHttpSession();

        memberSession.setAttribute(
                SessionConst.LOGIN_MEMBER_ID,
                member.getId()
        );
    }

    @Test
    @DisplayName("MEMBER의 정비 항목 등록 요청은 차단되며 데이터 비생성")
    void memberCannotCreateMaintenanceItem() throws Exception {

        //given
        Long beforeCount = countMaintenanceItems();

        //when
        mockMvc.perform(
                        post("/admin/maintenance-items")
                                .session(memberSession)
                                .param("name", "권한이 없는 정비 항목")
                                .param("price", "50000")
                )
                .andExpect(status().isForbidden());

        //then
        entityManager.flush();
        entityManager.clear();

        Long afterCount = countMaintenanceItems();

        assertThat(afterCount)
                .isEqualTo(beforeCount);
    }

    @Test
    @DisplayName("MEMBER의 항목 수정 요청을 차단되며 기존 데이터가 유지")
    void memberCannotUpdateMaintenanceItem() throws Exception {

        //given
        MaintenanceItem item = createMaintenanceItem(
                "엔진오일 교환",
                "엔진오일 교환 및 필터 교체",
                80000L
        );

        Long itemId = item.getId();

        //when
        mockMvc.perform(
                        post("/admin/maintenance-items/{id}/edit", itemId)
                                .session(memberSession)
                                .param("name", "변경된 정비 항목")
                                .param("description", "변경된 설명")
                                .param("estimatedPrice", "150000")
                )
                .andExpect(status().isForbidden());

        //then
        entityManager.flush();
        entityManager.clear();

        MaintenanceItem unchangedItem =
                entityManager.find(MaintenanceItem.class, itemId);

        assertThat(unchangedItem.getName())
                .isEqualTo("엔진오일 교환");

        assertThat(unchangedItem.getDescription())
                .isEqualTo("엔진오일 교환 및 필터 교체");

        assertThat(unchangedItem.getEstimatedPrice())
                .isEqualTo(80000L);
    }

    @Test
    @DisplayName("MEMBER의 정비 항목 활성화 요청은 차단되며 비활성 상태 유지")
    void memberCannotActivateMaintenanceItem() throws Exception {

        //given
        MaintenanceItem item = createMaintenanceItem(
                "브레이크 패드",
                "브레이크 패드 교체",
                120000L
        );

        item.deactivate();

        entityManager.flush();

        Long itemId = item.getId();

        //when
        mockMvc.perform(
                        post("/admin/maintenance-items/{id}/activate", itemId)
                                .session(memberSession)
                )
                .andExpect(status().isForbidden());

        //then
        entityManager.flush();
        entityManager.clear();

        MaintenanceItem unchangedItem =
                entityManager.find(MaintenanceItem.class, itemId);

        assertThat(unchangedItem.isActive())
                .isFalse();
    }

    @Test
    @DisplayName("MEMBER의 정비 항목 비활성화 요청은 차단되며 활성 상태가 유지")
    void memberCannotDeactivateMaintenanceItem() throws Exception {

        //given
        MaintenanceItem item = createMaintenanceItem(
                "타이어 교환",
                "타이어 교체",
                200000L
        );

        Long itemId = item.getId();

        //when
        mockMvc.perform(
                        post("/admin/maintenance-items/{id}/deactivate", itemId)
                                .session(memberSession)
                )
                .andExpect(status().isForbidden());

        //then
        entityManager.flush();
        entityManager.clear();

        MaintenanceItem unchangedItem =
                entityManager.find(MaintenanceItem.class, itemId);

        assertThat(unchangedItem.isActive())
                .isTrue();
    }

    @Test
    @DisplayName("MEMBER의 예약 확정 요청은 차단되며 PENDING 상태 유지")
    void memberCannotConfirmReservation() throws Exception {

        //given
        Reservation reservation = createReservation();

        Long reservationId = reservation.getId();

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.PENDING);

        //when
        mockMvc.perform(
                        post("/admin/reservations/{reservationId}/confirm", reservationId)
                                .session(memberSession)
                )
                .andExpect(status().isForbidden());

        //then
        entityManager.flush();
        entityManager.clear();

        Reservation unchangedReservation =
                entityManager.find(
                        Reservation.class,
                        reservationId
                );

        assertThat(unchangedReservation)
                .isNotNull();

        assertThat(unchangedReservation.getStatus())
                .isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("MEMBER의 예약 완료 요청은 차단되며 CONFIRMED 상태 유지")
    void memberCannotCompleteReservation() throws Exception {

        //given
        Reservation reservation = createReservation();

        reservation.confirm();

        entityManager.flush();

        Long reservationId = reservation.getId();

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);

        //when
        mockMvc.perform(
                        post("/admin/reservations/{reservationId}/complete", reservationId)
                                .session(memberSession)
                )
                .andExpect(status().isForbidden());

        //then
        entityManager.flush();
        entityManager.clear();

        Reservation unchangedReservation =
                entityManager.find(
                        Reservation.class,
                        reservationId
                );

        assertThat(unchangedReservation)
                .isNotNull();

        assertThat(unchangedReservation.getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);
    }

    private MaintenanceItem createMaintenanceItem(
            String name,
            String description,
            Long estimatedPrice
    ) {
        MaintenanceItem item = MaintenanceItem.create(
                name,
                description,
                estimatedPrice
        );

        entityManager.persist(item);
        entityManager.flush();

        return item;
    }

    private Long countMaintenanceItems() {

        return entityManager.createQuery(
                "SELECT COUNT(m) FROM MaintenanceItem m",
                        Long.class
                )
                .getSingleResult();
    }

    private Reservation createReservation() {

        Vehicle vehicle = Vehicle.create(
                member,
                "12허3434",
                "BMW",
                "Z4 M40i(G29)",
                2024
        );

        entityManager.persist(vehicle);

        Reservation reservation = Reservation.create(
                member,
                vehicle,
                LocalDate.now().plusDays(7),
                LocalTime.of(14, 0)
        );

        entityManager.persist(reservation);
        entityManager.flush();

        return reservation;
    }
}
