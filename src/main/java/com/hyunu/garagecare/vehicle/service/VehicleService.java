package com.hyunu.garagecare.vehicle.service;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.exception.MemberNotFoundException;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import com.hyunu.garagecare.vehicle.dto.VehicleCreateRequest;
import com.hyunu.garagecare.vehicle.dto.VehicleResponse;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import com.hyunu.garagecare.vehicle.dto.admin.AdminVehicleDetailResponse;
import com.hyunu.garagecare.vehicle.dto.admin.AdminVehicleListResponse;
import com.hyunu.garagecare.vehicle.exception.VehicleNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleService {

    private static final int ADMIN_VEHICLE_PAGE_SIZE = 10;

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

    public Page<AdminVehicleListResponse> getAdminVehicles(
            int page
    ) {
        Pageable pageable = PageRequest.of(
                page,
                ADMIN_VEHICLE_PAGE_SIZE,
                Sort.by(
                        Sort.Order.desc("id")
                )
        );

        return vehicleRepository
                .findAll(pageable)
                .map(AdminVehicleListResponse::from);
    }

    public AdminVehicleDetailResponse getAdminVehicleDetail(
            Long vehicleId
    ) {
        Vehicle vehicle = vehicleRepository
                .findById(vehicleId)
                .orElseThrow(VehicleNotFoundException::new);

        return AdminVehicleDetailResponse.from(vehicle);
    }
}
