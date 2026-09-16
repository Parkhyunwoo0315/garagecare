package com.hyunu.garagecare.vehicle.service;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import com.hyunu.garagecare.vehicle.dto.VehicleCreateRequest;
import com.hyunu.garagecare.vehicle.dto.VehicleResponse;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
