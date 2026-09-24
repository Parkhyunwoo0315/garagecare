package com.hyunu.garagecare.admin.reservation.controller;

import com.hyunu.garagecare.reservation.dto.admin.AdminReservationListResponse;
import com.hyunu.garagecare.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public String reservations(
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<AdminReservationListResponse> reservations =
                reservationService.getAdminReservations(page);

        model.addAttribute(
                "reservations",
                reservations
        );

        return "admin/reservation/list";
    }
}
