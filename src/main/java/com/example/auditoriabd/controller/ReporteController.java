package com.example.auditoriabd.controller;

import com.example.auditoriabd.dto.ReporteAuditoriaView;
import com.example.auditoriabd.service.ReporteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auditorias/{id}")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/resultados")
    public String resultados(@PathVariable Integer id, Model model) {
        ReporteAuditoriaView reporte = reporteService.construirReporte(id);
        model.addAttribute("reporte", reporte);
        return "reporte/resultados";
    }

    @GetMapping("/resultados/ejecutivo")
    public String ejecutivo(@PathVariable Integer id, Model model) {
        ReporteAuditoriaView reporte = reporteService.construirReporte(id);
        model.addAttribute("reporte", reporte);
        return "reporte/ejecutivo";
    }
}
