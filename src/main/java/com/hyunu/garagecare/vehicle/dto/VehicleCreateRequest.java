package com.hyunu.garagecare.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleCreateRequest {

    @NotBlank(message = "차량번호를 입력해 주세요.")
    @Size(max = 20, message = "차량번호는 20자 이하여야 합니다.")
    private String vehicleNumber;

    @Size(max = 50, message = "제조사는 50자 이하여야 합니다.")
    private String manufacturer;

    @NotBlank(message = "차량 모델을 입력해 주세요.")
    @Size(max = 100, message = "차량 모델은 100자 이하여야 합니다.")
    private String model;

    private Integer modelYear;
}
