package com.hyunu.garagecare.admin.dashboard.service;

import com.hyunu.garagecare.admin.dashboard.dto.AdminDashboardResponse;
import com.hyunu.garagecare.maintenance.repository.MaintenanceItemRepository;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.reservation.domain.ReservationStatus;
import com.hyunu.garagecare.reservation.repository.ReservationRepository;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final MemberRepository memberRepository;
    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;
    private final MaintenanceItemRepository maintenanceItemRepository;

    public AdminDashboardResponse getDashboard() {

        Long memberCount = memberRepository.count();

        Long vehicleCount = vehicleRepository.count();

        Long reservationCount = reservationRepository.count();

        Long pendingReservationCount = reservationRepository.countByStatus(
                ReservationStatus.PENDING
        );

        Long confirmedReservationCount = reservationRepository.countByStatus(
                ReservationStatus.CONFIRMED
        );

        Long completedReservationCount = reservationRepository.countByStatus(
                ReservationStatus.COMPLETED
        );

        Long canceledReservationCount = reservationRepository.countByStatus(
                ReservationStatus.CANCELED
        );

        Long maintenanceItemCount = maintenanceItemRepository.count();

        Long activeMaintenanceItemCount = maintenanceItemRepository.countByActiveTrue();

        Long inactiveMaintenanceItemCount = maintenanceItemRepository.countByActiveFalse();

        return new AdminDashboardResponse(
                memberCount,
                vehicleCount,
                reservationCount,
                pendingReservationCount,
                confirmedReservationCount,
                completedReservationCount,
                canceledReservationCount,
                maintenanceItemCount,
                activeMaintenanceItemCount,
                inactiveMaintenanceItemCount
        );
    }
}
