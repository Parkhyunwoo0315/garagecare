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
import com.hyunu.garagecare.reservation.dto.ReservationDetailResponse;
import com.hyunu.garagecare.reservation.dto.ReservationListResponse;
import com.hyunu.garagecare.reservation.exception.*;
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

    public List<ReservationListResponse> getReservations(Long memberId) {
        return reservationRepository.findAllByMemberIdOrderByReservationDateDescReservationTimeDesc(memberId)
                .stream()
                .map(ReservationListResponse::from)
                .toList();
    }

    public ReservationDetailResponse getReservationDetail(
            Long memberId,
            Long reservationId) {
        Reservation reservation = reservationRepository
                .findDetailById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        validateReservationOwnship(
                memberId,
                reservation
        );

        return ReservationDetailResponse.from(reservation);
    }

    public List<VehicleOptionResponse> getMemberVehicles(Long memberId) {
        return vehicleRepository.findAllByMemberId(memberId)
                .stream()
                .map(VehicleOptionResponse::from)
                .toList();
    }

    public List<MaintenanceItemOptionResponse> getActiveMaintenanceItems() {
        return maintenanceItemRepository.findAllByActiveTrue()
                .stream()
                .map(MaintenanceItemOptionResponse::from)
                .toList();
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

    private void validateReservationOwnship(
            Long memberId,
            Reservation reservation
    ) {
        if (!reservation.getMember()
                .getId()
                .equals(memberId)) {
            throw new UnauthorizedReservationAccessException();
        }
    }
}