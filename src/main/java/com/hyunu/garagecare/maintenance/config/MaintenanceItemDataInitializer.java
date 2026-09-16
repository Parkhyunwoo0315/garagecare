package com.hyunu.garagecare.maintenance.config;

import com.hyunu.garagecare.maintenance.domain.MaintenanceItem;
import com.hyunu.garagecare.maintenance.repository.MaintenanceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class MaintenanceItemDataInitializer implements CommandLineRunner {

    private final MaintenanceItemRepository maintenanceItemRepository;

    @Override
    public void run(String... args) {

        if (maintenanceItemRepository.count() > 0) {
            return;
        }

        maintenanceItemRepository.save(
                MaintenanceItem.create(
                        "엔진오일 교환",
                        "엔진오일 및 오일필터 교환",
                        100_000L
                )
        );

        maintenanceItemRepository.save(
                MaintenanceItem.create(
                        "브레이크 점검",
                        "브레이크 패드 및 디스크 상태 점검",
                        50_000L
                )
        );

        maintenanceItemRepository.save(
                MaintenanceItem.create(
                        "타이어 점검",
                        "타이어 마모도 및 공기압 점검",
                        30_000L
                )
        );

        maintenanceItemRepository.save(
                MaintenanceItem.create(
                        "배터리 점검",
                        "배터리 전압 및 상태 점검",
                        20_000L
                )
        );

        maintenanceItemRepository.save(
                MaintenanceItem.create(
                        "냉각수 점검",
                        "냉각수 상태 및 누수 여부 점검",
                        30_000L
                )
        );
    }
}