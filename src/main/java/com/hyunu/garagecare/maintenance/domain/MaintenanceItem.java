package com.hyunu.garagecare.maintenance.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "maintenance_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_maintenance_item_name",
                        columnNames = "name"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MaintenanceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            length = 50
    )
    private String name;

    @Column(
            length = 500
    )
    private String description;

    @Column(
            name = "estimated_price"
    )
    private Long estimatedPrice;

    @Column(nullable = false)
    private boolean active;

    private MaintenanceItem(
            String name,
            String description,
            Long estimatedPrice
    ) {
        validateName(name);
        validateEstimatedPrice(estimatedPrice);

        this.name = name;
        this.description = description;
        this.estimatedPrice = estimatedPrice;
        this.active = true;
    }

    public static MaintenanceItem create(
            String name,
            String description,
            Long estimatedPrice
    ) {
        return new MaintenanceItem(
                name,
                description,
                estimatedPrice
        );
    }

    public void update(
            String name,
            String description,
            Long estimatedPrice
    ) {
        validateName(name);
        validateEstimatedPrice(estimatedPrice);

        this.name = name;
        this.description = description;
        this.estimatedPrice = estimatedPrice;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "정비 항목 이름은 필수입니다."
            );
        }
    }

    private void validateEstimatedPrice(
            Long estimatedPrice
    ) {
        if (estimatedPrice != null
                && estimatedPrice < 0) {

            throw new IllegalArgumentException(
                    "예상 가격은 0원 이상이어야 합니다."
            );
        }
    }
}