package com.hyunu.garagecare.vehicle.service;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.exception.MemberNotFoundException;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import com.hyunu.garagecare.vehicle.dto.VehicleCreateRequest;
import com.hyunu.garagecare.vehicle.dto.VehicleResponse;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createVehicle(
            Long memberId,
            VehicleCreateRequest request
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        Vehicle vehicle = Vehicle.create(
                member,
                request.getVehicleNumber(),
                request.getManufacturer(),
                request.getModel(),
                request.getModelYear()
        );

        return vehicleRepository.save(vehicle).getId();
    }

    public List<VehicleResponse> getMemberVehicles(Long memberId) {
        return vehicleRepository.findAllByMemberId(memberId)
                .stream()
                .map(VehicleResponse::from)
                .toList();
    }
}
