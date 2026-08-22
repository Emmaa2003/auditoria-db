package com.example.auditoriabd.dto.monitor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sub-indice de salud (IP, IM o IA) con su clasificacion y el detalle de las
 * metricas crudas de utilizacion que lo componen.
 */
public class IndicadorComponenteView {

    private final String nombre;
    private final BigDecimal valorSalud;
    private final String nivelTextoSalud;
    private final String nivelCssSalud;
    private final List<MetricaDetalleView> detalle;

    public IndicadorComponenteView(String nombre, BigDecimal valorSalud, String nivelTextoSalud,
                                    String nivelCssSalud, List<MetricaDetalleView> detalle) {
        this.nombre = nombre;
        this.valorSalud = valorSalud;
        this.nivelTextoSalud = nivelTextoSalud;
        this.nivelCssSalud = nivelCssSalud;
        this.detalle = detalle;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getValorSalud() {
        return valorSalud;
    }

    public String getNivelTextoSalud() {
        return nivelTextoSalud;
    }

    public String getNivelCssSalud() {
        return nivelCssSalud;
    }

    public List<MetricaDetalleView> getDetalle() {
        return detalle;
    }
}
