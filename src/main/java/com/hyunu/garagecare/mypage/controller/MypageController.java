
package com.hyunu.garagecare.mypage.controller;

import com.hyunu.garagecare.member.session.SessionConst;
import com.hyunu.garagecare.reservation.service.ReservationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hyunu.garagecare.reservation.dto.ReservationListResponse;
import com.hyunu.garagecare.vehicle.dto.VehicleOptionResponse;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MypageController {

    private final ReservationService reservationService;

    @GetMapping("/mypage")
    public String mypage(
            HttpServletRequest request,
            Model model
    ) {

        HttpSession session = request.getSession(false);

        if (session == null ||
                !(session.getAttribute(
                        SessionConst.LOGIN_MEMBER_ID
                ) instanceof Long memberId)) {

            return "redirect:/members/login"
                    + "?redirectURL=/mypage";
        }

        List<VehicleOptionResponse> vehicles =
                reservationService.getMemberVehicles(
                        memberId
                );

        Page<ReservationListResponse> reservations =
                reservationService.getReservations(
                        memberId,
                        0
                );

        model.addAttribute(
                "vehicleCount",
                vehicles.size()
        );

        model.addAttribute(
                "reservationCount",
                reservations.getTotalElements()
        );

        return "mypage/index";
    }
}
