package com.hyunu.garagecare.reservation.domain;

import com.hyunu.garagecare.member.domain.Member;
import com.hyunu.garagecare.reservation.exception.InvalidReservationStatusException;
import com.hyunu.garagecare.vehicle.domain.Vehicle;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "reservation_time", nullable = false)
    private LocalTime reservationTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @OneToMany(
            mappedBy = "reservation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private final List<ReservationItem> reservationItems = new ArrayList<>();

    private Reservation(
            Member member,
            Vehicle vehicle,
            LocalDate reservationDate,
            LocalTime reservationTime
    ) {
        this.member = member;
        this.vehicle = vehicle;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.status = ReservationStatus.PENDING;
    }

    public static Reservation create(
            Member member,
            Vehicle vehicle,
            LocalDate reservationDate,
            LocalTime reservationTime
    ) {
        return new Reservation(
                member,
                vehicle,
                reservationDate,
                reservationTime
        );
    }

    public void addReservationItem(ReservationItem reservationItem) {
        reservationItems.add(reservationItem);
        reservationItem.assignReservation(this);
    }

    public void cancel() {
        if (status == ReservationStatus.CANCELED) {
            return;
        }

        if (status == ReservationStatus.COMPLETED) {
            throw new InvalidReservationStatusException(
                    "완료된 예약은 취소할 수 없습니다."
            );
        }

        this.status = ReservationStatus.CANCELED;
    }
}
