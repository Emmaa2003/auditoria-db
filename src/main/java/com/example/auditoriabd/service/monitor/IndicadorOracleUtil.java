package com.example.auditoriabd.service.monitor;

import com.example.auditoriabd.entity.monitor.EstadoSalud;
import com.example.auditoriabd.entity.monitor.NivelAlerta;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Escalas y formulas del Monitor de Salud de Oracle.
 *
 * <p>El documento guia define IP (Indicador de Procesos) como un porcentaje de
 * UTILIZACION donde mas alto = peor (seccion 7: 0-69 Normal ... 95-100
 * Critico), pero el ejemplo del indice combinado (seccion 19) lo usa como si
 * mas alto = mejor (IP=82 contribuye a un ISBD "Saludable"). Para resolver
 * esa inconsistencia sin contradecir ninguna de las dos partes del documento,
 * esta clase distingue dos capas:
 * <ul>
 *   <li>{@link #claseUtilizacion(double)}: clasifica una metrica CRUDA de
 *       utilizacion/presion (mas alto = peor) con la escala literal de la
 *       seccion 7 - se usa para las alertas individuales por variable.</li>
 *   <li>{@link #invertirUtilizacion(BigDecimal)}: convierte esa metrica cruda
 *       en un sub-indice de SALUD 0-100 (mas alto = mejor) mediante
 *       {@code salud = 100 - utilizacion}. Los sub-indices IP/IM/IA que se
 *       combinan en el ISBD (seccion 17) son siempre valores de salud, nunca
 *       la utilizacion cruda - asi el ejemplo de la seccion 19 es coherente:
 *       su "IP=82" corresponde a esta clase a una utilizacion subyacente de
 *       ~18%, perfectamente "saludable".</li>
 * </ul>
 * Ademas, la seccion 20 exige que el indice global no pueda ocultar un
 * problema critico: {@link #forzarCriticoPorPiso(EstadoSalud, BigDecimal...)}
 * implementa el piso duro (cualquier sub-indice de salud &lt; 40 fuerza el
 * "estado real" a CRITICO independientemente del promedio ponderado).
 */
public final class IndicadorOracleUtil {

    /** Piso duro de sub-indice de salud (0-100) bajo el cual se fuerza el estado real a CRITICO. */
    public static final BigDecimal PISO_CRITICO_SUBINDICE = BigDecimal.valueOf(40);

    private IndicadorOracleUtil() {
    }

    // ---- Escala de metricas crudas de utilizacion/presion (seccion 7 y 21) ----

    public static NivelAlerta claseUtilizacion(double porcentaje) {
        if (porcentaje >= 95) return NivelAlerta.CRITICO;
        if (porcentaje >= 85) return NivelAlerta.ALTO;
        if (porcentaje >= 70) return NivelAlerta.ADVERTENCIA;
        return NivelAlerta.NORMAL;
    }

    public static String textoNivelAlerta(NivelAlerta nivel) {
        return switch (nivel) {
            case NORMAL -> "Normal";
            case ADVERTENCIA -> "Advertencia";
            case ALTO -> "Alto";
            case CRITICO -> "Crítico";
        };
    }

    public static String cssNivelAlerta(NivelAlerta nivel) {
        return switch (nivel) {
            case NORMAL -> "riesgo-bajo";
            case ADVERTENCIA -> "riesgo-medio";
            case ALTO -> "riesgo-alto";
            case CRITICO -> "riesgo-critico";
        };
    }

    // ---- Conversion utilizacion cruda -> sub-indice de salud ----

    /** salud = 100 - utilizacion, acotado a [0,100]. */
    public static BigDecimal invertirUtilizacion(BigDecimal porcentajeUtilizacion) {
        return clamp(BigDecimal.valueOf(100).subtract(porcentajeUtilizacion));
    }

    public static BigDecimal clamp(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO;
        if (valor.compareTo(BigDecimal.valueOf(100)) > 0) return BigDecimal.valueOf(100);
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    // ---- Escala del ISBD (seccion 18, 5 niveles) ----

    public static EstadoSalud estadoIsbd(BigDecimal isbd) {
        double v = isbd.doubleValue();
        if (v >= 90) return EstadoSalud.OPTIMO;
        if (v >= 75) return EstadoSalud.SALUDABLE;
        if (v >= 60) return EstadoSalud.ADVERTENCIA;
        if (v >= 40) return EstadoSalud.DEGRADADO;
        return EstadoSalud.CRITICO;
    }

    public static String textoEstadoSalud(EstadoSalud estado) {
        return switch (estado) {
            case OPTIMO -> "Óptimo";
            case SALUDABLE -> "Saludable";
            case ADVERTENCIA -> "Advertencia";
            case DEGRADADO -> "Degradado";
            case CRITICO -> "Crítico";
        };
    }

    /** Reusa la misma paleta riesgo-bajo/medio/alto/critico ya definida en fragments/layout.html. */
    public static String cssEstadoSalud(EstadoSalud estado) {
        return switch (estado) {
            case OPTIMO, SALUDABLE -> "riesgo-bajo";
            case ADVERTENCIA -> "riesgo-medio";
            case DEGRADADO -> "riesgo-alto";
            case CRITICO -> "riesgo-critico";
        };
    }

    /**
     * Sección 20: el ISBD global no debe ocultar un problema crítico. El
     * estado real se fuerza a CRITICO si cualquiera de los sub-índices de
     * salud recibidos cae bajo {@link #PISO_CRITICO_SUBINDICE}, O si existe
     * al menos una alerta individual en nivel CRITICO (sesión bloqueada,
     * datafile offline, tablespace casi lleno, redo log inválido...) -
     * ninguna de las dos señales por separado alcanza para "esconder" un
     * problema: un promedio ponderado saludable con una sola alerta crítica
     * activa igual se reporta como estado real CRITICO.
     */
    public static EstadoSalud aplicarPisoCritico(EstadoSalud estadoDeclarado, boolean hayAlertaCritica,
                                                  BigDecimal... subIndices) {
        if (hayAlertaCritica) {
            return EstadoSalud.CRITICO;
        }
        for (BigDecimal sub : subIndices) {
            if (sub.compareTo(PISO_CRITICO_SUBINDICE) < 0) {
                return EstadoSalud.CRITICO;
            }
        }
        return estadoDeclarado;
    }
}
