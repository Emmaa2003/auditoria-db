package com.example.auditoriabd.entity.monitor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Snapshot por ciclo de las variables CRUDAS del monitor de procesos
 * (seccion 6.2 de la guia), para poder responder con historico preguntas
 * puntuales como "¿los procesos están creciendo?" (seccion 23) - el
 * sub-indice IP ya combinado no alcanza para eso, hace falta la serie de
 * cada variable por separado.
 */
@Entity
@Table(name = "monitor_procesos")
public class MonitorProcesos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_monitor_procesos")
    private Integer idMonitorProcesos;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "indicador_procesos", nullable = false)
    private BigDecimal indicadorProcesos;

    @Column(name = "uso_procesos_pct", nullable = false)
    private BigDecimal usoProcesosPct;

    @Column(name = "uso_sesiones_pct", nullable = false)
    private BigDecimal usoSesionesPct;

    @Column(name = "sesiones_bloqueadas", nullable = false)
    private long sesionesBloqueadas;

    @Column(name = "operaciones_prolongadas", nullable = false)
    private long operacionesProlongadas;

    @PrePersist
    protected void alCrear() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }

    public Integer getIdMonitorProcesos() {
        return idMonitorProcesos;
    }

    public void setIdMonitorProcesos(Integer idMonitorProcesos) {
        this.idMonitorProcesos = idMonitorProcesos;
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

    public BigDecimal getUsoProcesosPct() {
        return usoProcesosPct;
    }

    public void setUsoProcesosPct(BigDecimal usoProcesosPct) {
        this.usoProcesosPct = usoProcesosPct;
    }

    public BigDecimal getUsoSesionesPct() {
        return usoSesionesPct;
    }

    public void setUsoSesionesPct(BigDecimal usoSesionesPct) {
        this.usoSesionesPct = usoSesionesPct;
    }

    public long getSesionesBloqueadas() {
        return sesionesBloqueadas;
    }

    public void setSesionesBloqueadas(long sesionesBloqueadas) {
        this.sesionesBloqueadas = sesionesBloqueadas;
    }

    public long getOperacionesProlongadas() {
        return operacionesProlongadas;
    }

    public void setOperacionesProlongadas(long operacionesProlongadas) {
        this.operacionesProlongadas = operacionesProlongadas;
    }
}
