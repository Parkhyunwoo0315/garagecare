package com.hyunu.garagecare.vehicle.controller;

import com.hyunu.garagecare.member.session.SessionConst;
import com.hyunu.garagecare.vehicle.dto.VehicleCreateRequest;
import com.hyunu.garagecare.vehicle.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public String vehicles(
            HttpSession session,
            Model model
    ) {
        Long memberId = getLoginMemberId(session);

        model.addAttribute(
                "vehicles",
                vehicleService.getMemberVehicles(memberId)
        );

        return "vehicle/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute(
                "form",
                new VehicleCreateRequest()
        );

        return "vehicle/create-form";
    }

    @PostMapping
    public String create(
            @Valid
            @ModelAttribute("form")
            VehicleCreateRequest request,
            BindingResult bindingResult,
            HttpSession session
    ) {
        if (bindingResult.hasErrors()) {
            return "vehicle/create-form";
        }

        Long memberId = getLoginMemberId(session);

        vehicleService.createVehicle(
                memberId,
                request
        );

        return "redirect:/vehicles";
    }

    private Long getLoginMemberId(HttpSession session) {
        return (Long) session.getAttribute(
                SessionConst.LOGIN_MEMBER_ID
        );
    }
}
