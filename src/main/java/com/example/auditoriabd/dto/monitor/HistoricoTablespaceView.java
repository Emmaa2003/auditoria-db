package com.example.auditoriabd.dto.monitor;

import com.example.auditoriabd.entity.monitor.MonitorTablespace;

import java.util.List;

/** Serie historica (cronologica) de un tablespace, para el grafico "¿se están llenando?" (sección 23). */
public class HistoricoTablespaceView {

    private final String nombre;
    private final List<MonitorTablespace> puntos;

    public HistoricoTablespaceView(String nombre, List<MonitorTablespace> puntos) {
        this.nombre = nombre;
        this.puntos = puntos;
    }

    public String getNombre() {
        return nombre;
    }

    public List<MonitorTablespace> getPuntos() {
        return puntos;
    }
}
