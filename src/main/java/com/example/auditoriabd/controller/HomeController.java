package com.example.auditoriabd.controller;

import com.example.auditoriabd.service.PublicoDashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final PublicoDashboardService publicoDashboardService;

    public HomeController(PublicoDashboardService publicoDashboardService) {
        this.publicoDashboardService = publicoDashboardService;
    }

    @GetMapping("/")
    public String landing(Model model) {
        model.addAttribute("metricas", publicoDashboardService.obtenerMetricas());
        return "landing";
    }

    @GetMapping("/panel")
    public String panel() {
        return "panel";
    }
}
