package com.hyunu.garagecare.admin.maintenance.controller;

import com.hyunu.garagecare.maintenance.dto.admin.AdminMaintenanceItemListResponse;
import com.hyunu.garagecare.maintenance.dto.admin.AdminMaintenanceItemRequest;
import com.hyunu.garagecare.maintenance.service.MaintenanceItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/maintenance-items")
public class AdminMaintenanceItemController {

    private final MaintenanceItemService maintenanceItemService;

    @GetMapping
    public String maintenanceItems(
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<AdminMaintenanceItemListResponse> maintenanceItems =
                maintenanceItemService
                        .getAdminMaintenanceItems(page);

        model.addAttribute(
                "maintenanceItems",
                maintenanceItems
        );

        return "admin/maintenance/list";
    }

    @GetMapping("/new")
    public String createForm(
            Model model
    ) {
        model.addAttribute(
                "form",
                new AdminMaintenanceItemRequest()
        );

        model.addAttribute(
                "formMode",
                "create"
        );

        return "admin/maintenance/form";
    }

    @PostMapping
    public String create(
            @Valid
            @ModelAttribute("form")
            AdminMaintenanceItemRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "formMode",
                    "create"
            );

            return "admin/maintenance/form";
        }

        maintenanceItemService
                .createMaintenanceItem(request);

        return "redirect:/admin/maintenance-items";
    }

    @GetMapping("/{maintenanceItemId}/edit")
    public String updateForm(
            @PathVariable Long maintenanceItemId,
            Model model
    ) {
        model.addAttribute(
                "form",
                maintenanceItemService
                        .getAdminMaintenanceItem(
                                maintenanceItemId
                        )
        );

        model.addAttribute(
                "maintenanceItemId",
                maintenanceItemId
        );

        model.addAttribute(
                "formMode",
                "edit"
        );

        return "admin/maintenance/form";
    }

    @PostMapping("/{maintenanceItemId}/edit")
    public String update(
            @PathVariable long maintenanceItemId,
            @Valid
            @ModelAttribute("form")
            AdminMaintenanceItemRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "maintenanceItemId",
                    maintenanceItemId
            );

            model.addAttribute(
                    "formMode",
                    "edit"
            );

            return "admin/maintenance/form";
        }

        maintenanceItemService
                .updateMaintenanceItem(
                        maintenanceItemId,
                        request
                );

        return "redirect:/admin/maintenance-items";
    }

    @PostMapping("/{maintenanceItemId}/activate")
    public String activate(
            @PathVariable Long maintenanceItemId
    ) {
        maintenanceItemService
                .activateMaintenanceItem(
                        maintenanceItemId
                );

        return "redirect:/admin/maintenance-items";
    }

    @PostMapping("/{maintenanceItemId}/deactivate")
    public String deactivate(
            @PathVariable Long maintenanceItemId
    ) {
        maintenanceItemService
                .deactivateMaintenanceItem(
                        maintenanceItemId
                );

        return "redirect:/admin/maintenance-items";
    }
}
