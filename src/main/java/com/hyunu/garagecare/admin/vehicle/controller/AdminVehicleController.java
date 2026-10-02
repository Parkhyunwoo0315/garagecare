package com.hyunu.garagecare.admin.vehicle.controller;

import com.hyunu.garagecare.vehicle.dto.admin.AdminVehicleListResponse;
import com.hyunu.garagecare.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/vehicles")
public class AdminVehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public String vehicles(
            @RequestParam(defaultValue = "0") int page,
            Model model
    ){
        Page<AdminVehicleListResponse> vehicles =
                vehicleService.getAdminVehicles(page);

        model.addAttribute(
                "vehicles",
                vehicles
        );

        return "admin/vehicle/list";
    }

    @GetMapping("/{vehicleId}")
    public String vehicleDetail(
            @PathVariable Long vehicleId,
            Model model
    ){
        model.addAttribute(
                "vehicle",
                vehicleService.getAdminVehicleDetail(vehicleId)
        );

        return "admin/vehicle/detail";
    }
}
