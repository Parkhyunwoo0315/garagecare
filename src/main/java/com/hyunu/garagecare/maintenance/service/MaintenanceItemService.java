package com.hyunu.garagecare.maintenance.service;



import com.hyunu.garagecare.maintenance.domain.MaintenanceItem;
import com.hyunu.garagecare.maintenance.dto.admin.AdminMaintenanceItemListResponse;
import com.hyunu.garagecare.maintenance.dto.admin.AdminMaintenanceItemRequest;
import com.hyunu.garagecare.maintenance.exception.MaintenanceItemNotFoundException;
import com.hyunu.garagecare.maintenance.repository.MaintenanceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceItemService {

    private static final int PAGE_SIZE = 10;

    private final MaintenanceItemRepository maintenanceItemRepository;

    public Page<AdminMaintenanceItemListResponse>
    getAdminMaintenanceItems(int page) {
        PageRequest pageable =
                PageRequest.of(
                        page,
                        PAGE_SIZE
                );

        return maintenanceItemRepository
                .findAll(pageable)
                .map(AdminMaintenanceItemListResponse::from);
    }

    public AdminMaintenanceItemRequest
    getAdminMaintenanceItem(Long maintenanceItemId) {

        MaintenanceItem maintenanceItem =
                findMaintenanceItem(
                        maintenanceItemId
                );

        return new AdminMaintenanceItemRequest(
                maintenanceItem.getName(),
                maintenanceItem.getDescription(),
                maintenanceItem.getEstimatedPrice()
        );
    }

    @Transactional
    public Long createMaintenanceItem(
            AdminMaintenanceItemRequest request) {
        MaintenanceItem maintenanceItem =
                MaintenanceItem.create(
                        request.getName(),
                        request.getDescription(),
                        request.getEstimatedPrice()
                );

        return maintenanceItemRepository
                .save(maintenanceItem)
                .getId();
    }

    @Transactional
    public void updateMaintenanceItem(
            Long maintenanceItemId,
            AdminMaintenanceItemRequest request
    ) {
        MaintenanceItem maintenanceItem =
                findMaintenanceItem(
                        maintenanceItemId
                );

        maintenanceItem.update(
                request.getName(),
                request.getDescription(),
                request.getEstimatedPrice()
        );
    }

    @Transactional
    public void activateMaintenanceItem(
            Long maintenanceItemId
    ) {
        MaintenanceItem maintenanceItem =
                findMaintenanceItem(
                        maintenanceItemId
                );

        maintenanceItem.activate();
    }

    @Transactional
    public void deactivateMaintenanceItem(
            Long maintenanceItemId
    ) {
        MaintenanceItem maintenanceItem =
                findMaintenanceItem(
                        maintenanceItemId
                );

        maintenanceItem.deactivate();
    }

    private MaintenanceItem findMaintenanceItem(
            Long maintenanceItemId
    ) {
        return maintenanceItemRepository
                .findById(maintenanceItemId)
                .orElseThrow(
                        MaintenanceItemNotFoundException::new
                );
    }
}
