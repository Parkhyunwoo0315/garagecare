package com.hyunu.garagecare.reservation.service;

import com.hyunu.garagecare.maintenance.domain.MaintenanceItem;
import com.hyunu.garagecare.maintenance.dto.MaintenanceItemOptionResponse;
import com.hyunu.garagecare.maintenance.exception.InactiveMaintenanceItemException;
import com.hyunu.garagecare.maintenance.exception.MaintenanceItemNotFoundException;
import com.hyunu.garagecare.maintenance.repository.MaintenanceItemRepository;
import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.exception.MemberNotFoundException;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.reservation.domain.Reservation;
import com.hyunu.garagecare.reservation.domain.ReservationItem;
import com.hyunu.garagecare.reservation.dto.ReservationCreateRequest;
import com.hyunu.garagecare.reservation.exception.DuplicateMaintenanceItemException;
import com.hyunu.garagecare.reservation.exception.EmptyMaintenanceItemException;
import com.hyunu.garagecare.reservation.exception.UnauthorizedVehicleAccessException;
import com.hyunu.garagecare.reservation.repository.ReservationRepository;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import com.hyunu.garagecare.vehicle.dto.VehicleOptionResponse;
import com.hyunu.garagecare.vehicle.exception.VehicleNotFoundException;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceItemRepository maintenanceItemRepository;

    @Transactional
    public Long createReservation(
            Long memberId,
            ReservationCreateRequest request
    ) {

        Member member = findMember(memberId);

        Vehicle vehicle = findVehicle(request.getVehicleId());

        validateVehicleOwnership(member, vehicle);

        List<Long> maintenanceItemIds =
                request.getMaintenanceItemIds();

        validateMaintenanceItemIds(maintenanceItemIds);

        List<MaintenanceItem> maintenanceItems =
                findMaintenanceItems(maintenanceItemIds);

        validateActiveMaintenanceItems(maintenanceItems);

        Reservation reservation = Reservation.create(
                        member,
                        vehicle,
                        request.getReservationDate(),
                        request.getReservationTime()
        );

        addReservationItems(
                reservation,
                maintenanceItems
        );

        Reservation savedReservation = reservationRepository.save(reservation);

        return savedReservation.getId();
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
    }

    private Vehicle findVehicle(Long vehicleId) {
        return vehicleRepository.findById(vehicleId).orElseThrow(VehicleNotFoundException::new);
    }

    private void validateVehicleOwnership(
            Member member,
            Vehicle vehicle
    ) {
        if (!vehicle.isOwnedBy(member.getId())) {
            throw new UnauthorizedVehicleAccessException();
        }
    }

    private void validateMaintenanceItemIds(
            List<Long> maintenanceItemIds
    ) {
        if (maintenanceItemIds == null || maintenanceItemIds.isEmpty()) {
            throw new EmptyMaintenanceItemException();
        }

        if (new HashSet<>(maintenanceItemIds).size() != maintenanceItemIds.size()) {
            throw new DuplicateMaintenanceItemException();
        }
    }

    private List<MaintenanceItem> findMaintenanceItems(List<Long> maintenanceItemIds) {
        List<MaintenanceItem> maintenanceItems =
                maintenanceItemRepository.findAllById(
                        maintenanceItemIds
                );

        if (maintenanceItems.size() != maintenanceItemIds.size()) {
            throw new MaintenanceItemNotFoundException();
        }

        return maintenanceItems;
    }

    private void validateActiveMaintenanceItems(List<MaintenanceItem> maintenanceItems) {
        boolean containsInactiveItem =
                maintenanceItems.stream().anyMatch(
                                maintenanceItem ->
                                        !maintenanceItem.isActive()
                );

        if (containsInactiveItem) {
            throw new InactiveMaintenanceItemException();
        }
    }

    private void addReservationItems(
            Reservation reservation,
            List<MaintenanceItem> maintenanceItems
    ) {
        for (MaintenanceItem maintenanceItem : maintenanceItems) {
            ReservationItem reservationItem =
                    ReservationItem.create(
                            maintenanceItem
                    );

            reservation.addReservationItem(
                    reservationItem
            );
        }
    }

    @Transactional(readOnly = true)
    public List<VehicleOptionResponse> getMemberVehicles(Long memberId) {
        return vehicleRepository.findAllMemberId(memberId)
                .stream()
                .map(VehicleOptionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceItemOptionResponse> getActiveMaintenanceItems() {
        return maintenanceItemRepository.findAllByActiveTrue()
                .stream()
                .map(MaintenanceItemOptionResponse::from)
                .toList();
    }
}