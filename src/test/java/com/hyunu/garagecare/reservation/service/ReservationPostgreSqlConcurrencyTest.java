package com.hyunu.garagecare.reservation.service;

import com.hyunu.garagecare.maintenance.domain.MaintenanceItem;
import com.hyunu.garagecare.maintenance.repository.MaintenanceItemRepository;
import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.member.repository.MemberRepository;
import com.hyunu.garagecare.reservation.dto.ReservationCreateRequest;
import com.hyunu.garagecare.reservation.exception.DuplicateVehicleReservationException;
import com.hyunu.garagecare.reservation.exception.ReservationConstraintMatcher;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import com.hyunu.garagecare.vehicle.repository.VehicleRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("pgtest")
class ReservationPostgreSqlConcurrencyTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    MaintenanceItemRepository maintenanceItemRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void onlyOneConcurrentReservationSucceeds() throws Exception {

        String suffix = UUID.randomUUID()
                .toString()
                .substring(0, 8);

        Member member = memberRepository.save(
                Member.create(
                        "동시성테스트",
                        "race-" + suffix + "@example.com",
                        "encoded-password"
                )
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(
                        member,
                        "T" + suffix,
                        "현대",
                        "아반떼",
                        2022
                )
        );

        MaintenanceItem item = maintenanceItemRepository.save(
                MaintenanceItem.create(
                        "동시성 정비 " + suffix,
                        "테스트용 정비 항목",
                        50000L
                )
        );

        LocalDate date = LocalDate.now().plusDays(30);
        LocalTime time = LocalTime.of(14, 0);

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Object> attempt = () -> {

            start.await();

            ReservationCreateRequest request =
                    new ReservationCreateRequest();

            request.setVehicleId(vehicle.getId());
            request.setReservationDate(date);
            request.setReservationTime(time);
            request.setMaintenanceItemIds(List.of(item.getId()));

            try {
                return reservationService.createReservation(
                        member.getId(),
                        request
                );
            } catch (RuntimeException exception) {
                return exception;
            }
        };

        try {
            Future<Object> first = executor.submit(attempt);
            Future<Object> second = executor.submit(attempt);

            start.countDown();

            Object result1 = first.get(15, TimeUnit.SECONDS);
            Object result2 = second.get(15, TimeUnit.SECONDS);

            List<Object> results = List.of(result1, result2);

            long successful = results.stream()
                    .filter(Long.class::isInstance)
                    .count();

            assertThat(successful).isEqualTo(1);

            Object failed = results.stream()
                    .filter(result -> !(result instanceof Long))
                    .findFirst()
                    .orElseThrow();

            assertThat(failed).isInstanceOfAny(
                    DuplicateVehicleReservationException.class,
                    DataIntegrityViolationException.class
            );

            if (failed instanceof DataIntegrityViolationException dbException) {
                assertThat(
                        ReservationConstraintMatcher
                                .isDuplicateVehicleSlot(dbException)
                ).isTrue();
            }

            Long count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM reservations
                    WHERE vehicle_id = ?
                      AND reservation_date = ?
                      AND reservation_time = ?
                      AND status <> 'CANCELED'
                    """,
                    Long.class,
                    vehicle.getId(),
                    date,
                    time
            );

            assertThat(count).isEqualTo(1L);

        } finally {
            executor.shutdownNow();
        }
    }
}