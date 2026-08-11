package com.hyunu.garagecare.maintenance.repository;

import com.hyunu.garagecare.maintenance.domain.MaintenanceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceItemRepository
        extends JpaRepository<MaintenanceItem, Long> {

    boolean existsByNameIgnoreCase(String name);
}