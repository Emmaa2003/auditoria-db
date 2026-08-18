package com.example.auditoriabd.dto;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Métricas agregadas y anónimas para la landing pública ("/").
 * No expone datos de organizaciones ni de auditorías individuales,
 * solo conteos y promedios calculados sobre lo que ya hay en la base de datos.
 */
public class PublicoMetricasView {

    private long organizacionesRegistradas;
    private long auditoriasFinalizadas;
    private long auditoriasEnProgreso;
    private long totalControlesEvaluados;
    private long totalDominios;

    private boolean hayResultados;
    private BigDecimal madurezPromedio;
    private BigDecimal riesgoPromedio;
    private String riesgoPromedioTexto;
    private String riesgoPromedioCss;

    private Map<String, Long> distribucionRiesgo = new LinkedHashMap<>();

    public long getOrganizacionesRegistradas() {
        return organizacionesRegistradas;
    }

    public void setOrganizacionesRegistradas(long organizacionesRegistradas) {
        this.organizacionesRegistradas = organizacionesRegistradas;
    }

    public long getAuditoriasFinalizadas() {
        return auditoriasFinalizadas;
    }

    public void setAuditoriasFinalizadas(long auditoriasFinalizadas) {
        this.auditoriasFinalizadas = auditoriasFinalizadas;
    }

    public long getAuditoriasEnProgreso() {
        return auditoriasEnProgreso;
    }

    public void setAuditoriasEnProgreso(long auditoriasEnProgreso) {
        this.auditoriasEnProgreso = auditoriasEnProgreso;
    }

    public long getTotalControlesEvaluados() {
        return totalControlesEvaluados;
    }

    public void setTotalControlesEvaluados(long totalControlesEvaluados) {
        this.totalControlesEvaluados = totalControlesEvaluados;
    }

    public long getTotalDominios() {
        return totalDominios;
    }

    public void setTotalDominios(long totalDominios) {
        this.totalDominios = totalDominios;
    }

    public boolean isHayResultados() {
        return hayResultados;
    }

    public void setHayResultados(boolean hayResultados) {
        this.hayResultados = hayResultados;
    }

    public BigDecimal getMadurezPromedio() {
        return madurezPromedio;
    }

    public void setMadurezPromedio(BigDecimal madurezPromedio) {
        this.madurezPromedio = madurezPromedio;
    }

    public BigDecimal getRiesgoPromedio() {
        return riesgoPromedio;
    }

    public void setRiesgoPromedio(BigDecimal riesgoPromedio) {
        this.riesgoPromedio = riesgoPromedio;
    }

    public String getRiesgoPromedioTexto() {
        return riesgoPromedioTexto;
    }

    public void setRiesgoPromedioTexto(String riesgoPromedioTexto) {
        this.riesgoPromedioTexto = riesgoPromedioTexto;
    }

    public String getRiesgoPromedioCss() {
        return riesgoPromedioCss;
    }

    public void setRiesgoPromedioCss(String riesgoPromedioCss) {
        this.riesgoPromedioCss = riesgoPromedioCss;
    }

    public Map<String, Long> getDistribucionRiesgo() {
        return distribucionRiesgo;
    }

    public void setDistribucionRiesgo(Map<String, Long> distribucionRiesgo) {
        this.distribucionRiesgo = distribucionRiesgo;
    }
}
