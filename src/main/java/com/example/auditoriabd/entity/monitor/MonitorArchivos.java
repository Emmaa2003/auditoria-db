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
 * Snapshot por ciclo de las variables CRUDAS del monitor de archivos
 * (seccion 14 de la guia). El detalle por tablespace vive aparte en
 * {@link MonitorTablespace} porque son N filas por ciclo, no 1.
 */
@Entity
@Table(name = "monitor_archivos")
public class MonitorArchivos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_monitor_archivos")
    private Integer idMonitorArchivos;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "indicador_archivos", nullable = false)
    private BigDecimal indicadorArchivos;

    @Column(name = "datafiles_online", nullable = false)
    private long datafilesOnline;

    @Column(name = "datafiles_total", nullable = false)
    private long datafilesTotal;

    @Column(name = "tempfiles_online", nullable = false)
    private long tempfilesOnline;

    @Column(name = "tempfiles_total", nullable = false)
    private long tempfilesTotal;

    @Column(name = "redologs_invalidos", nullable = false)
    private long redologsInvalidos;

    @PrePersist
    protected void alCrear() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }

    public Integer getIdMonitorArchivos() {
        return idMonitorArchivos;
    }

    public void setIdMonitorArchivos(Integer idMonitorArchivos) {
        this.idMonitorArchivos = idMonitorArchivos;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public BigDecimal getIndicadorArchivos() {
        return indicadorArchivos;
    }

    public void setIndicadorArchivos(BigDecimal indicadorArchivos) {
        this.indicadorArchivos = indicadorArchivos;
    }

    public long getDatafilesOnline() {
        return datafilesOnline;
    }

    public void setDatafilesOnline(long datafilesOnline) {
        this.datafilesOnline = datafilesOnline;
    }

    public long getDatafilesTotal() {
        return datafilesTotal;
    }

    public void setDatafilesTotal(long datafilesTotal) {
        this.datafilesTotal = datafilesTotal;
    }

    public long getTempfilesOnline() {
        return tempfilesOnline;
    }

    public void setTempfilesOnline(long tempfilesOnline) {
        this.tempfilesOnline = tempfilesOnline;
    }

    public long getTempfilesTotal() {
        return tempfilesTotal;
    }

    public void setTempfilesTotal(long tempfilesTotal) {
        this.tempfilesTotal = tempfilesTotal;
    }

    public long getRedologsInvalidos() {
        return redologsInvalidos;
    }

    public void setRedologsInvalidos(long redologsInvalidos) {
        this.redologsInvalidos = redologsInvalidos;
    }
}
