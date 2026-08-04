package com.example.auditoriabd.dto;

import java.math.BigDecimal;

public class ResultadoControlView {
    private String codigo;
    private String nombre;
    private String dominio;
    private String peso;
    private short nivelMadurez;
    private BigDecimal porcentajeCumplimiento;
    private BigDecimal riesgoControl;
    private BigDecimal riesgoControlPct;
    private String nivelRiesgoTexto;
    private String nivelRiesgoCss;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDominio() {
        return dominio;
    }

    public void setDominio(String dominio) {
        this.dominio = dominio;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    public short getNivelMadurez() {
        return nivelMadurez;
    }

    public void setNivelMadurez(short nivelMadurez) {
        this.nivelMadurez = nivelMadurez;
    }

    public BigDecimal getPorcentajeCumplimiento() {
        return porcentajeCumplimiento;
    }

    public void setPorcentajeCumplimiento(BigDecimal porcentajeCumplimiento) {
        this.porcentajeCumplimiento = porcentajeCumplimiento;
    }

    public BigDecimal getRiesgoControl() {
        return riesgoControl;
    }

    public void setRiesgoControl(BigDecimal riesgoControl) {
        this.riesgoControl = riesgoControl;
    }

    public BigDecimal getRiesgoControlPct() {
        return riesgoControlPct;
    }

    public void setRiesgoControlPct(BigDecimal riesgoControlPct) {
        this.riesgoControlPct = riesgoControlPct;
    }

    public String getNivelRiesgoTexto() {
        return nivelRiesgoTexto;
    }

    public void setNivelRiesgoTexto(String nivelRiesgoTexto) {
        this.nivelRiesgoTexto = nivelRiesgoTexto;
    }

    public String getNivelRiesgoCss() {
        return nivelRiesgoCss;
    }

    public void setNivelRiesgoCss(String nivelRiesgoCss) {
        this.nivelRiesgoCss = nivelRiesgoCss;
    }
}
