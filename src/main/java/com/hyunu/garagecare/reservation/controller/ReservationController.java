package com.hyunu.garagecare.reservation.controller;

import com.hyunu.garagecare.member.session.SessionConst;
import com.hyunu.garagecare.reservation.dto.ReservationCreateRequest;
import com.hyunu.garagecare.reservation.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public String create(
            @Valid
            @ModelAttribute("form")
            ReservationCreateRequest request,
            BindingResult bindingResult,
            HttpSession session,
            Model model
    ) {
        Long memberId = getLonginMemberId(session);

        if (bindingResult.hasErrors()) {
            addReservationOptions(
                    memberId,
                    model
            );
        }
        Long reservationId =
                reservationService.createReservation(
                        memberId,
                        request
                );
        return "redirect:/reservations/" + reservationId;
    }

    @PostMapping("/{reservationId}/cancel")
    public String cancelReservation(
            @PathVariable Long reservationId,
            HttpSession session
    ) {
        Long memberId = getLonginMemberId(session);

        reservationService.cancelReservation(
                memberId,
                reservationId
        );

        return "redirect:/reservations/" + reservationId;
    }

    @GetMapping("/new")
    public String createForm(
            HttpSession session,
            Model model
    ) {
        Long memberId = getLonginMemberId(session);

        model.addAttribute(
                "form",
                new ReservationCreateRequest()
        );

        addReservationOptions(
                memberId,
                model
        );
        return "reservation/create-form";
    }

    @GetMapping
    public String reservations(
            HttpSession session,
            Model model
    ) {
        Long memberId = getLonginMemberId(session);

        model.addAttribute(
                "reservations",
                reservationService.getReservations(memberId)
        );
        return "reservation/list";
    }

    @GetMapping("/{reservationId}")
    public String reservationDetail(
            @PathVariable Long reservationId,
            HttpSession session,
            Model model
    ) {
        Long memberId = getLonginMemberId(session);

        model.addAttribute(
                "reservation",
                reservationService.getReservationDetail(
                        memberId,
                        reservationId
                )
        );
        return "reservation/detail";
    }

    private Long getLonginMemberId(HttpSession session) {
        return (Long) session.getAttribute(
            SessionConst.LOGIN_MEMBER_ID
        );
    }

    private void addReservationOptions(
            Long memberId,
            Model model
    ) {
        model.addAttribute(
                "vehicles",
                reservationService.getMemberVehicles(memberId)
        );

        model.addAttribute(
                "maintenanceItems",
                reservationService.getActiveMaintenanceItems()
        );
    }
}
