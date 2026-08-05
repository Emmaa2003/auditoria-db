package com.example.auditoriabd.controller;

import com.example.auditoriabd.dto.ReporteAuditoriaView;
import com.example.auditoriabd.dto.ResultadoControlView;
import com.example.auditoriabd.service.ReporteService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Controller
@RequestMapping("/auditorias/{id}")
public class ReporteController {

    private static final String CSV_SEPARADOR = ";";

    private final ReporteService reporteService;
    private final ITemplateEngine templateEngine;

    public ReporteController(ReporteService reporteService, ITemplateEngine templateEngine) {
        this.reporteService = reporteService;
        this.templateEngine = templateEngine;
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

    @GetMapping("/resultados/excel")
    public ResponseEntity<byte[]> excel(@PathVariable Integer id) {
        ReporteAuditoriaView reporte = reporteService.construirReporte(id);

        StringBuilder csv = new StringBuilder();
        csv.append('﻿');
        csv.append("Código").append(CSV_SEPARADOR)
                .append("Nombre").append(CSV_SEPARADOR)
                .append("Dominio").append(CSV_SEPARADOR)
                .append("Peso").append(CSV_SEPARADOR)
                .append("% Cumplimiento").append(CSV_SEPARADOR)
                .append("Nivel de Madurez").append(CSV_SEPARADOR)
                .append("Riesgo (%)").append(CSV_SEPARADOR)
                .append("Nivel de Riesgo").append("\r\n");

        for (ResultadoControlView c : reporte.getControles()) {
            csv.append(csvField(c.getCodigo())).append(CSV_SEPARADOR)
                    .append(csvField(c.getNombre())).append(CSV_SEPARADOR)
                    .append(csvField(c.getDominio())).append(CSV_SEPARADOR)
                    .append(csvField(c.getPeso())).append(CSV_SEPARADOR)
                    .append(csvField(String.valueOf(c.getPorcentajeCumplimiento()))).append(CSV_SEPARADOR)
                    .append(csvField(String.valueOf(c.getNivelMadurez()))).append(CSV_SEPARADOR)
                    .append(csvField(String.valueOf(c.getRiesgoControlPct()))).append(CSV_SEPARADOR)
                    .append(csvField(c.getNivelRiesgoTexto())).append("\r\n");
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("resultados-auditoria-" + id + ".csv", StandardCharsets.UTF_8)
                .build());
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .headers(headers)
                .body(bytes);
    }

    private String csvField(String valor) {
        if (valor == null) {
            return "";
        }
        boolean necesitaComillas = valor.contains(CSV_SEPARADOR) || valor.contains("\"")
                || valor.contains("\n") || valor.contains("\r");
        String escapado = valor.replace("\"", "\"\"");
        return necesitaComillas ? "\"" + escapado + "\"" : escapado;
    }

    @GetMapping("/resultados/ejecutivo/pdf")
    public ResponseEntity<byte[]> ejecutivoPdf(@PathVariable Integer id) {
        ReporteAuditoriaView reporte = reporteService.construirReporte(id);

        Context context = new Context(Locale.forLanguageTag("es-CR"));
        context.setVariable("reporte", reporte);
        String html = templateEngine.process("reporte/ejecutivo-pdf", context);

        byte[] pdf = renderPdf(html);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("reporte-auditoria-" + id + ".pdf", StandardCharsets.UTF_8)
                .build());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .headers(headers)
                .body(pdf);
    }

    private byte[] renderPdf(String html) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        } catch (com.lowagie.text.DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte", e);
        }
    }
}
