package com.hyunu.garagecare.admin.dashboard.controller;

import com.hyunu.garagecare.admin.dashboard.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public String dashboard(Model model){

        model.addAttribute(
                "dashboard",
                adminDashboardService.getDashboard()
        );

        return "admin/dashboard";
    }
}
