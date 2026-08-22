package com.example.auditoriabd.controller.monitor;

import com.example.auditoriabd.service.monitor.MonitorOracleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/monitor")
public class MonitorOracleController {

    private final MonitorOracleService monitorOracleService;

    public MonitorOracleController(MonitorOracleService monitorOracleService) {
        this.monitorOracleService = monitorOracleService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("monitor", monitorOracleService.medirYRegistrar());
        return "monitor/index";
    }
}
