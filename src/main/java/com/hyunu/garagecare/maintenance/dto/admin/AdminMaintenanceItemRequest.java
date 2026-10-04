package com.hyunu.garagecare.maintenance.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminMaintenanceItemRequest {

    @NotBlank(message = "정비 항목 이름을 입력해 주세요.")
    @Size(
            max = 50,
            message = "정비 항목 이름은 50자 이하여야 합니다."
    )
    private String Name;

    @Size(
            max = 500,
            message = "설명은 500자 이하여야 합니다."
    )
    private String description;

    @PositiveOrZero(
            message = "예상 가격은 0원 이상이여야 합니다."
    )
    private Long estimatedPrice;

    public AdminMaintenanceItemRequest() {
    }

    public AdminMaintenanceItemRequest(
            String name,
            String description,
            Long estimatedPrice
    ) {
        this.Name = name;
        this.description = description;
        this.estimatedPrice = estimatedPrice;
    }
}
