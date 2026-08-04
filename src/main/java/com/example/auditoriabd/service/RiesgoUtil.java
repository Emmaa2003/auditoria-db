package com.example.auditoriabd.service;

import com.example.auditoriabd.entity.PesoControl;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Reglas de negocio de conversión porcentaje-de-cumplimiento -> madurez -> riesgo,
 * y la escala de interpretación de riesgo (0-25 Bajo, 26-50 Medio, 51-75 Alto, 76-100 Crítico)
 * usada tanto en el cálculo como en el mapa de calor / tablas de resultados.
 */
public final class RiesgoUtil {

    private RiesgoUtil() {
    }

    public static short calcularNivelMadurez(BigDecimal porcentajeCumplimiento) {
        double pct = porcentajeCumplimiento.doubleValue();
        if (pct <= 0) return 0;
        if (pct <= 20) return 1;
        if (pct <= 40) return 2;
        if (pct <= 60) return 3;
        if (pct <= 80) return 4;
        return 5;
    }

    public static BigDecimal calcularRiesgoControl(short nivelMadurez, PesoControl peso) {
        BigDecimal factor = BigDecimal.valueOf(5 - nivelMadurez).divide(BigDecimal.valueOf(5), 6, RoundingMode.HALF_UP);
        return factor.multiply(BigDecimal.valueOf(peso.getValorNumerico())).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal normalizarRiesgoPorcentaje(BigDecimal riesgoPromedio) {
        return riesgoPromedio.divide(BigDecimal.valueOf(3), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static String nivelRiesgoTexto(BigDecimal porcentajeRiesgo) {
        double pct = porcentajeRiesgo.doubleValue();
        if (pct <= 25) return "Bajo";
        if (pct <= 50) return "Medio";
        if (pct <= 75) return "Alto";
        return "Crítico";
    }

    public static String nivelRiesgoCss(BigDecimal porcentajeRiesgo) {
        double pct = porcentajeRiesgo.doubleValue();
        if (pct <= 25) return "riesgo-bajo";
        if (pct <= 50) return "riesgo-medio";
        if (pct <= 75) return "riesgo-alto";
        return "riesgo-critico";
    }
}
