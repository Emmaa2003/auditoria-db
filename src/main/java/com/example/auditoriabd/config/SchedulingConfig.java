package com.example.auditoriabd.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita la ejecución periódica de {@link MonitorSchedulerService} sin
 * tocar la clase principal de arranque de la aplicación.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
