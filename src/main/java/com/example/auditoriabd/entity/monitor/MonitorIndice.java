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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Punto historico del calculo de indicadores del Monitor de Salud de Oracle
 * (una fila por cada medicion, ya sea disparada por el job programado o por
 * una visita a /monitor).
 */
@Entity
@Table(name = "monitor_indice")
public class MonitorIndice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_monitor_indice")
    private Integer idMonitorIndice;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "indicador_procesos", nullable = false)
    private BigDecimal indicadorProcesos;

    @Column(name = "indicador_memoria", nullable = false)
    private BigDecimal indicadorMemoria;

    @Column(name = "indicador_archivos", nullable = false)
    private BigDecimal indicadorArchivos;

    @Column(name = "indice_salud", nullable = false)
    private BigDecimal indiceSalud;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoSalud estado;

    @PrePersist
    protected void alCrear() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }

    public Integer getIdMonitorIndice() {
        return idMonitorIndice;
    }

    public void setIdMonitorIndice(Integer idMonitorIndice) {
        this.idMonitorIndice = idMonitorIndice;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public BigDecimal getIndicadorProcesos() {
        return indicadorProcesos;
    }

    public void setIndicadorProcesos(BigDecimal indicadorProcesos) {
        this.indicadorProcesos = indicadorProcesos;
    }

    public BigDecimal getIndicadorMemoria() {
        return indicadorMemoria;
    }

    public void setIndicadorMemoria(BigDecimal indicadorMemoria) {
        this.indicadorMemoria = indicadorMemoria;
    }

    public BigDecimal getIndicadorArchivos() {
        return indicadorArchivos;
    }

    public void setIndicadorArchivos(BigDecimal indicadorArchivos) {
        this.indicadorArchivos = indicadorArchivos;
    }

    public BigDecimal getIndiceSalud() {
        return indiceSalud;
    }

    public void setIndiceSalud(BigDecimal indiceSalud) {
        this.indiceSalud = indiceSalud;
    }

    public EstadoSalud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSalud estado) {
        this.estado = estado;
    }
}
