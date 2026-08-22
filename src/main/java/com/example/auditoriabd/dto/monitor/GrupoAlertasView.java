package com.example.auditoriabd.dto.monitor;

import com.example.auditoriabd.entity.monitor.MonitorAlerta;

import java.util.List;

/**
 * Implementa "relacionar las alertas" (paso explicito de la guia general de
 * monitoreo, antes de "presentar la informacion"): agrupa las alertas de la
 * medicion actual por componente. Cuando un mismo componente dispara 2 o mas
 * alertas en el mismo ciclo, se marca como posible causa comun (es mas
 * probable que varios sintomas del mismo subsistema compartan origen que
 * sintomas de subsistemas distintos).
 */
public class GrupoAlertasView {

    private final String componente;
    private final List<MonitorAlerta> alertas;
    private final boolean posibleCausaComun;

    public GrupoAlertasView(String componente, List<MonitorAlerta> alertas, boolean posibleCausaComun) {
        this.componente = componente;
        this.alertas = alertas;
        this.posibleCausaComun = posibleCausaComun;
    }

    public String getComponente() {
        return componente;
    }

    public List<MonitorAlerta> getAlertas() {
        return alertas;
    }

    public boolean isPosibleCausaComun() {
        return posibleCausaComun;
    }
}
