package com.hyunu.garagecare.reservation.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ReservationCreateRequest {

    @NotNull(message = "차량을 선택해 주세요.")
    private Long vehicleId;

    @NotNull(message = "예약 날짜를 선택해 주세요.")
    @FutureOrPresent(message = "예약 날짜는 오늘 이후여야 합니다.")
    private LocalDate reservationDate;

    @NotNull(message = "예약 시간을 선택해 주세요.")
    private LocalTime reservationTime;

    @NotEmpty(message = "정비 항목을 하나 이상 선택해 주세요.")
    private List<Long> maintenanceItemIds =  new ArrayList<>();
}
