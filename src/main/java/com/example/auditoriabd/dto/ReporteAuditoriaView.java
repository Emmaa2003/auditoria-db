package com.example.auditoriabd.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReporteAuditoriaView {
    private Integer idAuditoria;
    private String organizacionNombre;
    private String areaEvaluada;
    private LocalDate fechaAuditoria;
    private String auditorNombre;

    private BigDecimal madurezGeneral;
    private BigDecimal riesgoConfidencialidad;
    private BigDecimal riesgoIntegridad;
    private BigDecimal riesgoDisponibilidad;
    private BigDecimal indiceRiesgoGeneral;
    private String nivelRiesgoGeneralTexto;
    private String nivelRiesgoGeneralCss;

    private List<ResultadoControlView> controles;
    private List<ResultadoDominioView> dominios;
    private List<ResultadoControlView> rankingMenorMadurez;
    private List<ResultadoControlView> rankingMayorRiesgo;

    public Integer getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Integer idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public String getOrganizacionNombre() {
        return organizacionNombre;
    }

    public void setOrganizacionNombre(String organizacionNombre) {
        this.organizacionNombre = organizacionNombre;
    }

    public String getAreaEvaluada() {
        return areaEvaluada;
    }

    public void setAreaEvaluada(String areaEvaluada) {
        this.areaEvaluada = areaEvaluada;
    }

    public LocalDate getFechaAuditoria() {
        return fechaAuditoria;
    }

    public void setFechaAuditoria(LocalDate fechaAuditoria) {
        this.fechaAuditoria = fechaAuditoria;
    }

    public String getAuditorNombre() {
        return auditorNombre;
    }

    public void setAuditorNombre(String auditorNombre) {
        this.auditorNombre = auditorNombre;
    }

    public BigDecimal getMadurezGeneral() {
        return madurezGeneral;
    }

    public void setMadurezGeneral(BigDecimal madurezGeneral) {
        this.madurezGeneral = madurezGeneral;
    }

    public BigDecimal getRiesgoConfidencialidad() {
        return riesgoConfidencialidad;
    }

    public void setRiesgoConfidencialidad(BigDecimal riesgoConfidencialidad) {
        this.riesgoConfidencialidad = riesgoConfidencialidad;
    }

    public BigDecimal getRiesgoIntegridad() {
        return riesgoIntegridad;
    }

    public void setRiesgoIntegridad(BigDecimal riesgoIntegridad) {
        this.riesgoIntegridad = riesgoIntegridad;
    }

    public BigDecimal getRiesgoDisponibilidad() {
        return riesgoDisponibilidad;
    }

    public void setRiesgoDisponibilidad(BigDecimal riesgoDisponibilidad) {
        this.riesgoDisponibilidad = riesgoDisponibilidad;
    }

    public BigDecimal getIndiceRiesgoGeneral() {
        return indiceRiesgoGeneral;
    }

    public void setIndiceRiesgoGeneral(BigDecimal indiceRiesgoGeneral) {
        this.indiceRiesgoGeneral = indiceRiesgoGeneral;
    }

    public String getNivelRiesgoGeneralTexto() {
        return nivelRiesgoGeneralTexto;
    }

    public void setNivelRiesgoGeneralTexto(String nivelRiesgoGeneralTexto) {
        this.nivelRiesgoGeneralTexto = nivelRiesgoGeneralTexto;
    }

    public String getNivelRiesgoGeneralCss() {
        return nivelRiesgoGeneralCss;
    }

    public void setNivelRiesgoGeneralCss(String nivelRiesgoGeneralCss) {
        this.nivelRiesgoGeneralCss = nivelRiesgoGeneralCss;
    }

    public List<ResultadoControlView> getControles() {
        return controles;
    }

    public void setControles(List<ResultadoControlView> controles) {
        this.controles = controles;
    }

    public List<ResultadoDominioView> getDominios() {
        return dominios;
    }

    public void setDominios(List<ResultadoDominioView> dominios) {
        this.dominios = dominios;
    }

    public List<ResultadoControlView> getRankingMenorMadurez() {
        return rankingMenorMadurez;
    }

    public void setRankingMenorMadurez(List<ResultadoControlView> rankingMenorMadurez) {
        this.rankingMenorMadurez = rankingMenorMadurez;
    }

    public List<ResultadoControlView> getRankingMayorRiesgo() {
        return rankingMayorRiesgo;
    }

    public void setRankingMayorRiesgo(List<ResultadoControlView> rankingMayorRiesgo) {
        this.rankingMayorRiesgo = rankingMayorRiesgo;
    }
}
