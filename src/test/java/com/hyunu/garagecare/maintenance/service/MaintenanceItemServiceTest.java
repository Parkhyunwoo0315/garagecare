package com.hyunu.garagecare.maintenance.service;

import com.hyunu.garagecare.maintenance.domain.MaintenanceItem;
import com.hyunu.garagecare.maintenance.dto.admin.AdminMaintenanceItemListResponse;
import com.hyunu.garagecare.maintenance.dto.admin.AdminMaintenanceItemRequest;
import com.hyunu.garagecare.maintenance.exception.MaintenanceItemNotFoundException;
import com.hyunu.garagecare.maintenance.repository.MaintenanceItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class MaintenanceItemServiceTest {

    @Autowired
    MaintenanceItemService maintenanceItemService;

    @Autowired
    MaintenanceItemRepository maintenanceItemRepository;

    @Test
    @DisplayName("관리자는 정비 항목 등록 가능")
    void createMaintenanceItem() {

        //given
        AdminMaintenanceItemRequest request =
                new AdminMaintenanceItemRequest(
                        "엔진 오일 필터 교환",
                        "엔진 오일 상태 확인 및 필터 교환",
                        60000L
                );

        //when
        Long maintenanceItemId =
                maintenanceItemService
                        .createMaintenanceItem(request);

        //then
        MaintenanceItem maintenanceItem =
                maintenanceItemRepository
                        .findById(maintenanceItemId)
                        .orElseThrow();

        assertThat(maintenanceItem.getName())
                .isEqualTo("엔진 오일 필터 교환");

        assertThat(maintenanceItem.getDescription())
                .isEqualTo("엔진 오일 상태 확인 및 필터 교환");

        assertThat(maintenanceItem.getEstimatedPrice())
                .isEqualTo(60000L);

        assertThat(maintenanceItem.isActive())
                .isTrue();
    }

    @Test
    @DisplayName("관리자는 활성 및 비활성 정비 항목을 모두 조회 가능")
    void getAdminMaintenanceItems() {

        //given
        MaintenanceItem activeItem =
                maintenanceItemRepository.save(
                        MaintenanceItem.create(
                                "와이퍼 교체",
                                "워셔액 용량 확인 및 와이퍼 교체",
                                40000L
                        )
                );

        MaintenanceItem inactiveItem =
                MaintenanceItem.create(
                        "에어컨 필터 교체",
                        "캐빈 필터 교체",
                        30000L
                );

        inactiveItem.deactivate();

        maintenanceItemRepository.save(
                inactiveItem
        );

        //when
        Page<AdminMaintenanceItemListResponse> result =
                maintenanceItemService
                        .getAdminMaintenanceItems(0);

        assertThat(result.getContent())
                .extracting(
                        AdminMaintenanceItemListResponse::maintenanceItemId
                )
                .contains(
                        activeItem.getId(),
                        inactiveItem.getId()
                );
    }

    @Test
    @DisplayName("정비 항목이 없으면 빈 페이지를 반환")
    void getEmptyAdminMaintenanceItems() {

        //given
        maintenanceItemRepository.deleteAll();
        maintenanceItemRepository.flush();

        //when
        Page<AdminMaintenanceItemListResponse> result =
                maintenanceItemService
                        .getAdminMaintenanceItems(0);

        //then
        assertThat(result.getContent())
                .isEmpty();
        assertThat(result.getTotalElements())
                .isZero();
    }

    @Test
    @DisplayName("관리자는 정비 항목의 기존 정보를 조회 가능")
    void getAdminMaintenanceItem() {

        //given
        MaintenanceItem maintenanceItem =
                maintenanceItemRepository.save(
                        MaintenanceItem.create(
                                "점화 플러그 교체",
                                "점화 플러그 상태 확인",
                                120000L
                        )
                );
        //when
        AdminMaintenanceItemRequest response =
                maintenanceItemService
                        .getAdminMaintenanceItem(
                                maintenanceItem.getId()
                        );

        //then
        assertThat(response.getName())
                .isEqualTo("점화 플러그 교체");
        assertThat(response.getDescription())
                .isEqualTo("점화 플러그 상태 확인");
        assertThat(response.getEstimatedPrice())
                .isEqualTo(120000L);
    }

    @Test
    @DisplayName("관리자는 정비 항목 정보를 수정 가능")
    void updateAdminMaintenanceItem() {

        //given
        MaintenanceItem maintenanceItem =
                maintenanceItemRepository.save(
                        MaintenanceItem.create(
                                "엔진오일",
                                "기존 설명",
                                70000L
                        )
                );

        AdminMaintenanceItemRequest request =
                new AdminMaintenanceItemRequest(
                        "엔진오일 교체",
                        "엔진오일 및 필터 교환",
                        90000L
                );

        //when
        maintenanceItemService.updateMaintenanceItem(
                maintenanceItem.getId(),
                request
        );

        //then
        MaintenanceItem updatedItem =
                maintenanceItemRepository
                        .findById(maintenanceItem.getId())
                        .orElseThrow();

        assertThat(updatedItem.getName())
                .isEqualTo("엔진오일 교체");
        assertThat(updatedItem.getDescription())
                .isEqualTo("엔진오일 및 필터 교환");
        assertThat(updatedItem.getEstimatedPrice())
                .isEqualTo(90000L);
    }

    @Test
    @DisplayName("관리자는 정비 항목을 비활성화 가능")
    void deactivatedAdminMaintenanceItem() {

        //given
        MaintenanceItem maintenanceItem =
                maintenanceItemRepository.save(
                        MaintenanceItem.create(
                                "휠 얼라이먼트",
                                "휠 정렬 상태 점검",
                                80000L
                        )
                );

        //when
        maintenanceItemService
                .deactivateMaintenanceItem(
                        maintenanceItem.getId()
                );

        //then
        MaintenanceItem result =
                maintenanceItemRepository
                        .findById(maintenanceItem.getId())
                        .orElseThrow();

        assertThat(result.isActive())
                .isFalse();
    }

    @Test
    @DisplayName("관리자는 비활성화한 정비 항목을 다시 활성화 가능")
    void activeAdminMaintenanceItem() {

        //given
        MaintenanceItem maintenanceItem =
                MaintenanceItem.create(
                        "배터리 교체",
                        "배터리 성능 확인 및 교체",
                        18000L
                );

        maintenanceItem.deactivate();

        maintenanceItemRepository.save(
                maintenanceItem
        );

        //when
        maintenanceItemService
                .activateMaintenanceItem(
                        maintenanceItem.getId()
                );

        //then
        MaintenanceItem result =
                maintenanceItemRepository
                        .findById(maintenanceItem.getId())
                        .orElseThrow();

        assertThat(result.isActive())
                .isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 정비 항목을 조회하면 예외 발생")
    void maintenanceItemNotFound() {

        //when & then
        assertThatThrownBy(
                () -> maintenanceItemService
                        .getAdminMaintenanceItem(
                                Long.MAX_VALUE
                        )
        )
                .isInstanceOf(
                        MaintenanceItemNotFoundException.class
                );
    }
}
