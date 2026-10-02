package com.hyunu.garagecare.vehicle.service;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import com.hyunu.garagecare.vehicle.dto.VehicleCreateRequest;
import com.hyunu.garagecare.vehicle.dto.VehicleResponse;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import com.hyunu.garagecare.vehicle.dto.admin.AdminVehicleDetailResponse;
import com.hyunu.garagecare.vehicle.dto.admin.AdminVehicleListResponse;
import com.hyunu.garagecare.vehicle.exception.VehicleNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class VehicleServiceTest {

    @Autowired
    VehicleService vehicleService;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("로그인 회원의 차량 등록 성공")
    void createVehicle() {

        // given
        Member member = memberRepository.save(
                Member.create(
                        "박현우",
                        "vehicle-create@test.com",
                        "password"
                )
        );

        VehicleCreateRequest request =
                new VehicleCreateRequest();

        request.setVehicleNumber("12가3456");
        request.setManufacturer("Mazda");
        request.setModel("RX-7 Type R (FD3S)");
        request.setModelYear(1999);

        // when
        Long vehicleId =
                vehicleService.createVehicle(
                        member.getId(),
                        request
                );

        // then
        Vehicle vehicle = vehicleRepository
                .findById(vehicleId)
                .orElseThrow();

        assertThat(vehicle.getMember().getId())
                .isEqualTo(member.getId());

        assertThat(vehicle.getVehicleNumber())
                .isEqualTo("12가3456");

        assertThat(vehicle.getManufacturer())
                .isEqualTo("Mazda");

        assertThat(vehicle.getModel())
                .isEqualTo("RX-7 Type R (FD3S)");

        assertThat(vehicle.getModelYear())
                .isEqualTo(1999);
    }

    @Test
    @DisplayName("회원별 등록 차량 조회")
    void getMemberVehicles() {

        // given
        Member member = memberRepository.save(
                Member.create(
                        "박현우",
                        "vehicle-list@test.com",
                        "password"
                )
        );

        Member otherMember = memberRepository.save(
                Member.create(
                        "다른 회원",
                        "vehicle-other@test.com",
                        "password"
                )
        );

        vehicleRepository.save(
                Vehicle.create(
                        member,
                        "12가3456",
                        "Toyota",
                        "Supra RZ (A80)",
                        1998
                )
        );

        vehicleRepository.save(
                Vehicle.create(
                        otherMember,
                        "34나5678",
                        "Mercedes-Benz",
                        "C 63 AMG (W204)",
                        2012
                )
        );

        // when
        List<VehicleResponse> vehicles =
                vehicleService.getMemberVehicles(
                        member.getId()
                );

        // then
        assertThat(vehicles).hasSize(1);

        VehicleResponse response =
                vehicles.get(0);

        assertThat(response.vehicleNumber())
                .isEqualTo("12가3456");

        assertThat(response.model())
                .isEqualTo("Supra RZ (A80)");
    }

    @Test
    @DisplayName("관리자는 전체 회원의 차량 목록을 조회 가능")
    void getAdminVehicles() {

        // given
        Member member1 = memberRepository.save(
                Member.create(
                        "박현우",
                        "admin-vehicle-list1@test.com",
                        "password"
                )
        );

        Member member2 = memberRepository.save(
                Member.create(
                        "박정우",
                        "admin-vehicle-list2@test.com",
                        "password"
                )
        );

        Vehicle vehicle1 = vehicleRepository.save(
                Vehicle.create(
                        member1,
                        "268가8986",
                        "Subaru",
                        "Impreza WRX STI (GC8)",
                        1998
                )
        );

        Vehicle vehicle2 = vehicleRepository.save(
                Vehicle.create(
                        member2,
                        "80아6412",
                        "Audi",
                        "RS2 Avant",
                        1995
                )
        );

        // when
        Page<AdminVehicleListResponse> result =
                vehicleService.getAdminVehicles(0);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isZero();

        assertThat(result.getContent())
                .extracting(AdminVehicleListResponse::vehicleId)
                .containsExactly(
                        vehicle2.getId(),
                        vehicle1.getId()
                );

        assertThat(result.getContent())
                .extracting(AdminVehicleListResponse::memberEmail)
                .containsExactly(
                        "admin-vehicle-list2@test.com",
                        "admin-vehicle-list1@test.com"
                );
    }

    @Test
    @DisplayName("차량이 없으면 관리자 차량 목록은 빈 페이지를 반환")
    void getEmptyAdminVehicles() {

        // when
        Page<AdminVehicleListResponse> result =
                vehicleService.getAdminVehicles(0);

        // then
        assertThat(result).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getNumber()).isZero();
    }

    @Test
    @DisplayName("관리자는 회원의 차량 상세 정보를 조회 가능")
    void getAdminVehicleDetail() {

        // given
        Member member = memberRepository.save(
                Member.create(
                        "박현우",
                        "admin-vehicle-detail@test.com",
                        "password"
                )
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        member,
                        "12가3456",
                        "Volkswagen",
                        "Golf GTI (Mk2)",
                        1990
                )
        );

        // when
        AdminVehicleDetailResponse result =
                vehicleService.getAdminVehicleDetail(
                        vehicle.getId()
                );

        // then
        assertThat(result.vehicleId())
                .isEqualTo(vehicle.getId());

        assertThat(result.vehicleNumber())
                .isEqualTo("12가3456");

        assertThat(result.manufacturer())
                .isEqualTo("Volkswagen");

        assertThat(result.model())
                .isEqualTo("Golf GTI (Mk2)");

        assertThat(result.modelYear())
                .isEqualTo(1990);

        assertThat(result.memberId())
                .isEqualTo(member.getId());

        assertThat(result.memberName())
                .isEqualTo("박현우");

        assertThat(result.memberEmail())
                .isEqualTo("admin-vehicle-detail@test.com");
    }
}
