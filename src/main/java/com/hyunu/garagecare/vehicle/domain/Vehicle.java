package com.hyunu.garagecare.vehicle.domain;

import com.hyunu.garagecare.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "vehicles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_vehicle_number",
                        columnNames = "vehicle_number"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "member_id",
            nullable = false
    )
    private Member member;

    @Column(
            name = "vehicle_number",
            nullable = false,
            length = 20
    )
    private String vehicleNumber;

    @Column(
            name = "manufacturer",
            length = 50
    )
    private String manufacturer;

    @Column(
            name = "model",
            nullable = false,
            length = 100
    )
    private String model;

    @Column(
            name = "model_year"
    )
    private Integer modelYear;

    private Vehicle(
            Member member,
            String vehicleNumber,
            String manufacturer,
            String model,
            Integer modelYear
    ) {
        this.member = member;
        this.vehicleNumber = vehicleNumber;
        this.manufacturer = manufacturer;
        this.model = model;
        this.modelYear = modelYear;
    }

    public static Vehicle create(
            Member member,
            String vehicleNumber,
            String manufacturer,
            String model,
            Integer modelYear
    ) {
        return new Vehicle(
                member,
                vehicleNumber,
                manufacturer,
                model,
                modelYear
        );
    }

    public boolean isOwnedBy(Long memberId) {
        return member != null
                && member.getId() != null
                && member.getId().equals(memberId);
    }
}
