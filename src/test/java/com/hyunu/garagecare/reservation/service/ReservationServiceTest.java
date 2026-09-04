package com.hyunu.garagecare.reservation.service;

import com.hyunu.garagecare.maintenance.domain.MaintenanceItem;
import com.hyunu.garagecare.maintenance.exception.InactiveMaintenanceItemException;
import com.hyunu.garagecare.maintenance.exception.MaintenanceItemNotFoundException;
import com.hyunu.garagecare.maintenance.repository.MaintenanceItemRepository;
import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.reservation.domain.Reservation;
import com.hyunu.garagecare.reservation.domain.ReservationStatus;
import com.hyunu.garagecare.reservation.dto.ReservationCreateRequest;
import com.hyunu.garagecare.reservation.dto.ReservationDetailResponse;
import com.hyunu.garagecare.reservation.dto.ReservationListResponse;
import com.hyunu.garagecare.reservation.exception.*;
import com.hyunu.garagecare.reservation.repository.ReservationRepository;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class ReservationServiceTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    MaintenanceItemRepository maintenanceItemRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    @DisplayName("예약 등록 성공")
    void createReservation() {

        //given
        Member member = Member.create(
                "박현우",
                "reservation@test.com",
                "encoded-password"
        );

        memberRepository.save(member);

        Vehicle vehicle = Vehicle.create(
                member,
                "12가3456",
                "Mercedes-Benz",
                "E350 아방가르드",
                2013
        );

        vehicleRepository.save(vehicle);

        MaintenanceItem maintenanceItem = MaintenanceItem.create(
                "엔진오일 교환",
                "엔진오일 필터를 교환하고 싶습니다.",
                700000L
        );

        maintenanceItemRepository.save(maintenanceItem);

        ReservationCreateRequest request = new ReservationCreateRequest();

        request.setVehicleId(vehicle.getId());
        request.setReservationDate(LocalDate.now().plusDays(1));
        request.setReservationTime(LocalTime.of(14, 0));
        request.setMaintenanceItemIds(List.of(maintenanceItem.getId()));

        //when
        Long reservationId = reservationService.createReservation(
                member.getId(),
                request
        );

        //then
        Reservation reservation = reservationRepository
                .findById(reservationId).orElseThrow();

        assertThat(reservation.getId()).isNotNull();
        assertThat(reservation.getMember().getId()).isEqualTo(member.getId());
        assertThat(reservation.getVehicle().getId()).isEqualTo(vehicle.getId());

        assertThat(reservation.getReservationItems()).hasSize(1);

        assertThat(reservation.getReservationItems()
                        .get(0)
                        .getMaintenanceItem()
                        .getId()
        ).isEqualTo(maintenanceItem.getId());
    }

    @Test
    @DisplayName("다른 회원의 차량으로 예약 불가")
    void createReservationWithAnotherMembersVehicle() {

        //given
        Member member1 = memberRepository.save(
                Member.create(
                "회원1",
                "member1@test.com",
                "password"
                )
        );

        Member member2 = memberRepository.save(
                Member.create(
                        "회원2",
                        "member2@test.com",
                        "password"
                )
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        member2,
                        "22나2222",
                        "현대",
                        "아반떼 CN7",
                        2021
                )
        );

        MaintenanceItem item = maintenanceItemRepository.save(
                MaintenanceItem.create(
                        "타이어 점검",
                        "타이어 상태를 점검하고 싶습니다.",
                        10000L
                )
        );

        ReservationCreateRequest request = createRequest(
                vehicle.getId(),
                List.of(
                        item.getId(),
                        item.getId()
                )
        );

        //when & them
        assertThatThrownBy(
                () -> reservationService
                        .createReservation(
                                member1.getId(),
                                request
                        )
        )
                .isInstanceOf(
                        UnauthorizedVehicleAccessException.class
                );

    }

    @Test
    @DisplayName("정비 항목이 없으면 예약 불가능")
    void createReservationWithoutMaintenanceItem() {

        //given
        Member member = memberRepository.save(
                Member.create(
                        "회원",
                        "empty@test.com",
                        "password"
                )
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        member,
                        "33다3333",
                        "제네시스",
                        "G80 RG3",
                        2022
                )
        );

        ReservationCreateRequest request = createRequest(
                vehicle.getId(),
                List.of()
        );

        //when & then
        assertThatThrownBy(
                () -> reservationService
                        .createReservation(
                                member.getId(),
                                request
                        )
        )
                .isInstanceOf(
                        EmptyMaintenanceItemException.class
                );

    }

    @Test
    @DisplayName("동일한 정비 항목을 중복 선택 불가능")
    void duplicateMaintenanceItem() {

        //given
        Member member = memberRepository.save(
                Member.create(
                        "회원",
                        "duplicate@test.com",
                        "password"
                )
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        member,
                        "44라4444",
                        "BMW",
                        "520i",
                        2021
                )
        );

        MaintenanceItem item = maintenanceItemRepository.save(
                MaintenanceItem.create(
                        "브레이크 점검",
                        "브레이크 상태를 점검하고 싶습니다.",
                        10000L
                )
        );

        ReservationCreateRequest request = createRequest(
                vehicle.getId(),
                List.of(
                        item.getId(),
                        item.getId()
                )
        );

        //when & then
        assertThatThrownBy(
                () -> reservationService
                        .createReservation(
                                member.getId(),
                                request
                        )
        )
                .isInstanceOf(
                        DuplicateMaintenanceItemException.class
                );

    }

    @Test
    @DisplayName("존재하지 않는 정비 항목으로 예약 불가능")
    void maintenanceItemNotFound() {

        //given
        Member member = memberRepository.save(
                Member.create(
                        "회원",
                        "notfound@test.com",
                        "password"
                )
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        member,
                        "55마 5555",
                        "Audi",
                        "A6 45 TFSI",
                        2020
                )
        );

        ReservationCreateRequest request = createRequest(
                vehicle.getId(),
                List.of(999999L)
        );

        //when & then
        assertThatThrownBy(
                () -> reservationService
                        .createReservation(
                                member.getId(),
                                request
                        )
        )
                .isInstanceOf(
                        MaintenanceItemNotFoundException.class
                );
    }

    @Test
    @DisplayName("비활성 정비 항목으로 예약 불가능")
    void inactiveMaintenanceItem() {
        //given
        Member member = memberRepository.save(
                Member.create(
                        "회원",
                        "inactive@test.com",
                        "password"
                )
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        member,
                        "66바6666",
                        "Lexus",
                        "ES300h",
                        2022
                )
        );

        MaintenanceItem item = maintenanceItemRepository.save(
                MaintenanceItem.create(
                        "비활성 항목",
                        "사용하지 않는 항목",
                        10000L
                )
        );

        item.deactivate();

        ReservationCreateRequest request = createRequest(
                vehicle.getId(),
                List.of(item.getId())
        );

        //when & then
        assertThatThrownBy(
                () -> reservationService
                        .createReservation(
                                member.getId(),
                                request
                        )
        )
                .isInstanceOf(
                        InactiveMaintenanceItemException.class
                );
    }

    @Test
    @DisplayName("회원의 예약 목록 조회")
    void getReservations() {

        //given
        Member member = memberRepository.save(
                Member.create(
                        "박현우",
                        "list@test.com",
                        "password"
                )
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        member,
                        "77사7777",
                        "Tesla",
                        "Model 3",
                        2024
                )
        );

        MaintenanceItem maintenanceItem = maintenanceItemRepository.save(
                MaintenanceItem.create(
                        "엔진오일 교환",
                        "엔진오일을 교환하고 싶습니다.",
                        70000L
                )
        );

        ReservationCreateRequest request = createRequest(
                vehicle.getId(),
                List.of(maintenanceItem.getId())
        );

        Long reservationId = reservationService.createReservation(
                member.getId(),
                request
        );

        //when
        Page<ReservationListResponse> result =
                reservationService.getReservations(member.getId(), 0);

        //then
        assertThat(result.getContent()).hasSize(2);
        ReservationListResponse response = result.getContent().get(0);
        assertThat(response.reservationId()).isEqualTo(reservationId);
        assertThat(response.vehicleNumber()).isEqualTo("77사7777");
    }

    @Test
    @DisplayName("예약이 없는 회원은 빈 목록을 반환")
    void getEmptyReservations() {

        //given
        Member member =memberRepository.save(
                Member.create(
                        "박현우",
                        "empty-list@test.com",
                        "password"
                )
        );

        //when
        Page<ReservationListResponse> result =
                reservationService.getReservations(member.getId(), 0);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("본인의 예약 상세 정보 조회")
    void getReservation() {

        //given
        Member member = memberRepository.save(
                Member.create(
                        "박현우",
                        "detail@test.com",
                        "password"
                )
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        member,
                        "88아8888",
                        "기아",
                        "쏘렌토 MQ4",
                        2023
                )
        );

        MaintenanceItem item1 = maintenanceItemRepository.save(
                MaintenanceItem.create(
                        "엔진오일 교환",
                        "엔진오일을 교환하고 싶습니다.",
                        70000L
                )
        );

        MaintenanceItem item2 = maintenanceItemRepository.save(
                MaintenanceItem.create(
                        "타이어 점검",
                        "타이어를 점검받고 싶습니다.",
                        10000L
                )
        );

        ReservationCreateRequest request = createRequest(
                vehicle.getId(),
                List.of(
                        item1.getId(),
                        item2.getId()
                )
        );

        Long reservationId = reservationService.createReservation(
                member.getId(),
                request
        );

        //when
        ReservationDetailResponse response =
                reservationService.getReservationDetail(
                        member.getId(),
                        reservationId
                );

        //then
        assertThat(response.reservationId()).isEqualTo(reservationId);
        assertThat(response.vehicleNumber()).isEqualTo("88아8888");
        assertThat(response.maintenanceItems()).hasSize(2);
    }

    @Test
    @DisplayName("존재하지 않는 예약을 조회하면 예외가 발생")
    void reservationNotFound() {

        //given
        Member member = memberRepository.save(
                Member.create(
                        "박현우",
                        "not-found-reservation@test.com",
                        "password"
                )
        );

        //when & then
        assertThatThrownBy(
                () -> reservationService.getReservationDetail(
                        member.getId(),
                        999999L
                )
        )
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    @DisplayName("다른 회원의 예약 상세 정보에 접근 불가능")
    void unauthorizedReservationAccess() {

        //given
        Member owner = memberRepository.save(
                Member.create(
                        "예약 소유자",
                        "owner@test.com",
                        "password"
                )
        );

        Member otherMember = memberRepository.save(
                Member.create(
                        "다른 회원",
                        "other@test.com",
                        "password"
                )
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        owner,
                        "99자9999",
                        "Volvo",
                        "XC60 B5",
                        2023
                )
        );

        MaintenanceItem maintenanceItem = maintenanceItemRepository.save(
                MaintenanceItem.create(
                        "엔진오일 교환",
                        "엔진오일을 교환하고 싶습니다.",
                        70000L
                )
        );

        ReservationCreateRequest request = createRequest(
                vehicle.getId(),
                List.of(maintenanceItem.getId())
        );

        Long reservationId = reservationService.createReservation(
                owner.getId(),
                request
        );

        //when & then
        assertThatThrownBy(
                () -> reservationService.getReservationDetail(
                        otherMember.getId(),
                        reservationId
                )
        )
                .isInstanceOf(
                        UnauthorizedReservationAccessException.class
                );
    }

    @Test
    @DisplayName("본인의 예약을 취소하면 상태가 CANCELED로 변경된다")
    void cancelReservation() {

        // given
        Member member = memberRepository.save(
                Member.create(
                        "박현우",
                        "cancel@test.com",
                        "password"
                )
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        member,
                        "12자1212",
                        "BMW",
                        "X5 xDrive40i",
                        2024
                )
        );

        MaintenanceItem maintenanceItem = maintenanceItemRepository.save(
                        MaintenanceItem.create(
                                "엔진오일 교환",
                                "엔진오일을 교환하고 싶습니다.",
                                70000L
                        )
        );

        ReservationCreateRequest request = createRequest(
                        vehicle.getId(),
                        List.of(
                                maintenanceItem.getId()
                        )
        );

        Long reservationId = reservationService.createReservation(
                        member.getId(),
                        request
                );

        // when
        reservationService.cancelReservation(
                member.getId(),
                reservationId
        );

        entityManager.flush();
        entityManager.clear();

        // then
        Reservation reservation = reservationRepository
                        .findById(reservationId)
                        .orElseThrow();

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("존재하지 않는 예약은 취소할 수 없다")
    void cancelReservationNotFound() {

        // given
        Member member = memberRepository.save(
                Member.create(
                        "박현우",
                        "cancel-not-found@test.com",
                        "password"
                )
        );

        // when & then
        assertThatThrownBy(
                () -> reservationService.cancelReservation(
                        member.getId(),
                        999999L
                )
        )
                .isInstanceOf(
                        ReservationNotFoundException.class
                );
    }

    @Test
    @DisplayName("다른 회원의 예약은 취소할 수 없다")
    void cannotCancelAnotherMembersReservation() {

        // given
        Member owner = memberRepository.save(
                Member.create(
                        "예약 소유자",
                        "cancel-owner@test.com",
                        "password"
                )
        );

        Member otherMember = memberRepository.save(
                Member.create(
                        "다른 회원",
                        "cancel-other@test.com",
                        "password"
                )
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        owner,
                        "34차3434",
                        "Mazda",
                        "MX-5",
                        2022
                )
        );

        MaintenanceItem maintenanceItem =
                maintenanceItemRepository.save(
                        MaintenanceItem.create(
                                "타이어 점검",
                                "타이어 상태를 점검하고 싶습니다.",
                                10000L
                        )
                );

        Long reservationId =
                reservationService.createReservation(
                        owner.getId(),
                        createRequest(
                                vehicle.getId(),
                                List.of(
                                        maintenanceItem.getId()
                                )
                        )
                );

        // when & then
        assertThatThrownBy(
                () -> reservationService.cancelReservation(
                        otherMember.getId(),
                        reservationId
                )
        )
                .isInstanceOf(
                        UnauthorizedReservationAccessException.class
                );

        Reservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow();

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("이미 취소된 예약을 다시 취소해도 상태가 유지된다")
    void cancelReservationTwice() {

        // given
        Member member = memberRepository.save(
                Member.create(
                        "홍길동",
                        "cancel-twice@test.com",
                        "password"
                )
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        member,
                        "56카5656",
                        "Lexus",
                        "ES 300h",
                        2022
                )
        );

        MaintenanceItem maintenanceItem =
                maintenanceItemRepository.save(
                        MaintenanceItem.create(
                                "브레이크 점검",
                                "브레이크 상태를 점검하고 싶습니다.",
                                20000L
                        )
                );

        Long reservationId =
                reservationService.createReservation(
                        member.getId(),
                        createRequest(
                                vehicle.getId(),
                                List.of(
                                        maintenanceItem.getId()
                                )
                        )
                );

        reservationService.cancelReservation(
                member.getId(),
                reservationId
        );

        // when
        reservationService.cancelReservation(
                member.getId(),
                reservationId
        );

        entityManager.flush();
        entityManager.clear();

        // then
        Reservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow();

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
    }

    private ReservationCreateRequest createRequest(
            Long vehicleId,
            List<Long> maintenanceItemIds
    ) {
        ReservationCreateRequest request = new ReservationCreateRequest();

        request.setVehicleId(vehicleId);
        request.setReservationDate(LocalDate.now().plusDays(1));
        request.setReservationTime(LocalTime.of(14, 0));
        request.setMaintenanceItemIds(maintenanceItemIds);

        return request;
    }
}
