package com.example.auditoriabd.config;

import com.example.auditoriabd.service.monitor.MonitorOracleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara periódicamente la medición del Monitor de Salud de Oracle
 * (sección 6 de la guía: "automatizar la medición"), independientemente de
 * si alguien está viendo la pantalla /monitor en ese momento. Un fallo
 * puntual de conexión no debe tumbar el scheduler, así que se atrapa y se
 * registra en el log en vez de propagarse.
 */
@Component
public class MonitorSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(MonitorSchedulerService.class);

    private final MonitorOracleService monitorOracleService;

    public MonitorSchedulerService(MonitorOracleService monitorOracleService) {
        this.monitorOracleService = monitorOracleService;
    }

    @Scheduled(fixedDelayString = "${monitor.oracle.polling-interval-ms}")
    public void medir() {
        try {
            monitorOracleService.medirYRegistrar();
        } catch (Exception e) {
            log.warn("No se pudo completar la medición programada del Monitor de Salud de Oracle: {}", e.getMessage());
        }
    }
}
