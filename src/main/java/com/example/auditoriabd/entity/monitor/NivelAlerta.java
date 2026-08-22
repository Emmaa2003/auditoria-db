package com.example.auditoriabd.entity.monitor;

/**
 * Escala de clasificacion de metricas crudas de utilizacion/presion y de alertas
 * individuales por componente, 4 niveles: 0-69 Normal, 70-84 Advertencia,
 * 85-94 Alto, 95-100 Critico.
 */
public enum NivelAlerta {
    NORMAL,
    ADVERTENCIA,
    ALTO,
    CRITICO
}
