package com.hyunu.garagecare.reservation;

import com.hyunu.garagecare.maintenance.domain.MaintenanceItem;
import com.hyunu.garagecare.maintenance.exception.InactiveMaintenanceItemException;
import com.hyunu.garagecare.maintenance.exception.MaintenanceItemNotFoundException;
import com.hyunu.garagecare.maintenance.repository.MaintenanceItemRepository;
import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.reservation.domain.Reservation;
import com.hyunu.garagecare.reservation.dto.ReservationCreateRequest;
import com.hyunu.garagecare.reservation.exception.DuplicateMaintenanceItemException;
import com.hyunu.garagecare.reservation.exception.EmptyMaintenanceItemException;
import com.hyunu.garagecare.reservation.exception.UnauthorizedVehicleAccessException;
import com.hyunu.garagecare.reservation.repository.ReservationRepository;
import com.hyunu.garagecare.reservation.service.ReservationService;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
                "벤츠",
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
                        "아반떼",
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
                        "G80",
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
                        "아우디",
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
                        "렉서스",
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
