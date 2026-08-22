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
 * Snapshot por ciclo de las variables CRUDAS del monitor de memoria
 * (secciones 9-11 de la guia), para poder responder con historico
 * "¿el consumo de memoria está aumentando?" (seccion 23).
 */
@Entity
@Table(name = "monitor_memoria")
public class MonitorMemoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_monitor_memoria")
    private Integer idMonitorMemoria;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "indicador_memoria", nullable = false)
    private BigDecimal indicadorMemoria;

    @Column(name = "cache_hit_pga_pct", nullable = false)
    private BigDecimal cacheHitPgaPct;

    @Column(name = "over_allocation_pga", nullable = false)
    private long overAllocationPga;

    @Column(name = "uso_sga_pct", nullable = false)
    private BigDecimal usoSgaPct;

    @PrePersist
    protected void alCrear() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }

    public Integer getIdMonitorMemoria() {
        return idMonitorMemoria;
    }

    public void setIdMonitorMemoria(Integer idMonitorMemoria) {
        this.idMonitorMemoria = idMonitorMemoria;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public BigDecimal getIndicadorMemoria() {
        return indicadorMemoria;
    }

    public void setIndicadorMemoria(BigDecimal indicadorMemoria) {
        this.indicadorMemoria = indicadorMemoria;
    }

    public BigDecimal getCacheHitPgaPct() {
        return cacheHitPgaPct;
    }

    public void setCacheHitPgaPct(BigDecimal cacheHitPgaPct) {
        this.cacheHitPgaPct = cacheHitPgaPct;
    }

    public long getOverAllocationPga() {
        return overAllocationPga;
    }

    public void setOverAllocationPga(long overAllocationPga) {
        this.overAllocationPga = overAllocationPga;
    }

    public BigDecimal getUsoSgaPct() {
        return usoSgaPct;
    }

    public void setUsoSgaPct(BigDecimal usoSgaPct) {
        this.usoSgaPct = usoSgaPct;
    }
}
