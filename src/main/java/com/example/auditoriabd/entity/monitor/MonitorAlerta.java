package com.example.auditoriabd.entity.monitor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Alerta individual generada por el Monitor de Salud de Oracle cuando una
 * metrica sale del rango NORMAL (seccion 21 de la guia: fecha, hora,
 * componente, variable, valor, umbral, nivel, descripcion).
 *
 * <p>Implementa "relacionar las alertas" (paso explicito de la guia general,
 * antes de "presentar la informacion"): cuando la misma condicion
 * (componente+variable+nivel) sigue activa en el ciclo siguiente dentro de
 * {@code VENTANA_PERSISTENCIA}, en vez de insertar una fila identica nueva
 * se ACTUALIZA esta misma fila (fechaHora avanza, ocurrencias++) - por eso
 * fechaHora ya no es {@code updatable = false}. {@code fechaPrimera} guarda
 * cuando empezo la condicion, para poder mostrar "persiste desde hace X" en
 * el dashboard en vez de tratarla como una alerta nueva cada vez.
 */
@Entity
@Table(name = "monitor_alerta")
public class MonitorAlerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_monitor_alerta")
    private Integer idMonitorAlerta;

    @Column(name = "fecha_primera", nullable = false, updatable = false)
    private LocalDateTime fechaPrimera;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "ocurrencias", nullable = false)
    private int ocurrencias = 1;

    @Column(name = "componente", nullable = false, length = 20)
    private String componente;

    @Column(name = "variable", nullable = false, length = 60)
    private String variable;

    @Column(name = "valor", nullable = false, length = 60)
    private String valor;

    @Column(name = "umbral", length = 60)
    private String umbral;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel", nullable = false, length = 20)
    private NivelAlerta nivel;

    @Column(name = "descripcion", nullable = false, length = 300)
    private String descripcion;

    @PrePersist
    protected void alCrear() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
        if (fechaPrimera == null) {
            fechaPrimera = fechaHora;
        }
    }

    public Integer getIdMonitorAlerta() {
        return idMonitorAlerta;
    }

    public void setIdMonitorAlerta(Integer idMonitorAlerta) {
        this.idMonitorAlerta = idMonitorAlerta;
    }

    public LocalDateTime getFechaPrimera() {
        return fechaPrimera;
    }

    public void setFechaPrimera(LocalDateTime fechaPrimera) {
        this.fechaPrimera = fechaPrimera;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getOcurrencias() {
        return ocurrencias;
    }

    public void setOcurrencias(int ocurrencias) {
        this.ocurrencias = ocurrencias;
    }

    public String getComponente() {
        return componente;
    }

    public void setComponente(String componente) {
        this.componente = componente;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getUmbral() {
        return umbral;
    }

    public void setUmbral(String umbral) {
        this.umbral = umbral;
    }

    public NivelAlerta getNivel() {
        return nivel;
    }

    public void setNivel(NivelAlerta nivel) {
        this.nivel = nivel;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
