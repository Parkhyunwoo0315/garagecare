package com.hyunu.garagecare.reservation.domain;

import com.hyunu.garagecare.maintenance.domain.MaintenanceItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "reservation_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reservation_maintenance_item",
                        columnNames = {
                                "reservation_id",
                                "maintenance_item_id"
                        }
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "reservation_id",
            nullable = false
    )
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "maintenance_item_id",
            nullable = false
    )
    private MaintenanceItem maintenanceItem;

    private ReservationItem(MaintenanceItem maintenanceItem) {
        this.maintenanceItem = maintenanceItem;
    }

    public static ReservationItem create(MaintenanceItem maintenanceItem) {
        return new ReservationItem(maintenanceItem);
    }

    void assignReservation(Reservation reservation) {
        this.reservation = reservation;
    }
}
