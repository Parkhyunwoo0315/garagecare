package com.hyunu.garagecare.admin.dashboard.service;

import com.hyunu.garagecare.admin.dashboard.dto.AdminDashboardResponse;
import com.hyunu.garagecare.maintenance.repository.MaintenanceItemRepository;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.reservation.domain.ReservationStatus;
import com.hyunu.garagecare.reservation.repository.ReservationRepository;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdminDashboardServiceTest {

    private MemberRepository memberRepository;
    private VehicleRepository vehicleRepository;
    private ReservationRepository reservationRepository;
    private MaintenanceItemRepository maintenanceItemRepository;

    private AdminDashboardService adminDashboardService;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        vehicleRepository = mock(VehicleRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        maintenanceItemRepository = mock(MaintenanceItemRepository.class);

        adminDashboardService = new AdminDashboardService(
                memberRepository,
                vehicleRepository,
                reservationRepository,
                maintenanceItemRepository
        );
    }

    @Test
    @DisplayName("관리자 대시보드의 전체 집계 오류 없음")
    void getDashboard() {

        //given
        when(memberRepository.count()).thenReturn(10L);
        when(vehicleRepository.count()).thenReturn(7L);
        when(reservationRepository.count()).thenReturn(12L);

        when(reservationRepository.countByStatus(
                ReservationStatus.PENDING
        )).thenReturn(3L);

        when(reservationRepository.countByStatus(
                ReservationStatus.CONFIRMED
        )).thenReturn(4L);

        when(reservationRepository.countByStatus(
                ReservationStatus.COMPLETED
        )).thenReturn(3L);

        when(reservationRepository.countByStatus(
                ReservationStatus.CANCELED
        )).thenReturn(2L);

        when(maintenanceItemRepository.count()).thenReturn(8L);
        when(maintenanceItemRepository.countByActiveTrue()).thenReturn(6L);
        when(maintenanceItemRepository.countByActiveFalse()).thenReturn(2L);

        //when
        AdminDashboardResponse dashboard =
                adminDashboardService.getDashboard();

        //then
        assertThat(dashboard.memberCount()).isEqualTo(10L);
        assertThat(dashboard.vehicleCount()).isEqualTo(7L);
        assertThat(dashboard.reservationCount()).isEqualTo(12L);

        assertThat(dashboard.pendingReservationCount()).isEqualTo(3L);
        assertThat(dashboard.confirmedReservationCount()).isEqualTo(4L);
        assertThat(dashboard.completedReservationCount()).isEqualTo(3L);
        assertThat(dashboard.canceledReservationCount()).isEqualTo(2L);

        assertThat(dashboard.maintenanceItemCount()).isEqualTo(8L);
        assertThat(dashboard.activeMaintenanceItemCount()).isEqualTo(6L);
        assertThat(dashboard.inactiveMaintenanceItemCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("집계 데이터가 없으면 모든 값은 0")
    void getDashboardWithNoData() {

        //given: Mockito의 long 기본 반환값은 0

        //when
        AdminDashboardResponse dashboard =
                adminDashboardService.getDashboard();

        //then
        assertThat(dashboard.memberCount()).isZero();
        assertThat(dashboard.vehicleCount()).isZero();
        assertThat(dashboard.reservationCount()).isZero();

        assertThat(dashboard.pendingReservationCount()).isZero();
        assertThat(dashboard.confirmedReservationCount()).isZero();
        assertThat(dashboard.completedReservationCount()).isZero();
        assertThat(dashboard.canceledReservationCount()).isZero();

        assertThat(dashboard.maintenanceItemCount()).isZero();
        assertThat(dashboard.activeMaintenanceItemCount()).isZero();
        assertThat(dashboard.inactiveMaintenanceItemCount()).isZero();
    }
}
