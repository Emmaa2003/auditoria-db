package com.example.auditoriabd.entity.monitor;

/**
 * Escala del Indice de Salud de la Base de Datos (ISBD), 5 niveles, 0-100.
 * 90-100 Optimo, 75-89 Saludable, 60-74 Advertencia, 40-59 Degradado, 0-39 Critico.
 */
public enum EstadoSalud {
    OPTIMO,
    SALUDABLE,
    ADVERTENCIA,
    DEGRADADO,
    CRITICO
}
