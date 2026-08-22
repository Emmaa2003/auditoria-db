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
 * Snapshot por ciclo Y por tablespace del porcentaje de uso (una fila por
 * cada tablespace en cada medicion). Es la tabla que resuelve, con
 * historico real, la pregunta explicita de la seccion 23 de la guia:
 * "¿los tablespaces se están llenando?".
 */
@Entity
@Table(name = "monitor_tablespace")
public class MonitorTablespace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_monitor_tablespace")
    private Integer idMonitorTablespace;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "tablespace_name", nullable = false, length = 30)
    private String tablespaceName;

    @Column(name = "porcentaje_usado", nullable = false)
    private BigDecimal porcentajeUsado;

    @PrePersist
    protected void alCrear() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }

    public Integer getIdMonitorTablespace() {
        return idMonitorTablespace;
    }

    public void setIdMonitorTablespace(Integer idMonitorTablespace) {
        this.idMonitorTablespace = idMonitorTablespace;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getTablespaceName() {
        return tablespaceName;
    }

    public void setTablespaceName(String tablespaceName) {
        this.tablespaceName = tablespaceName;
    }

    public BigDecimal getPorcentajeUsado() {
        return porcentajeUsado;
    }

    public void setPorcentajeUsado(BigDecimal porcentajeUsado) {
        this.porcentajeUsado = porcentajeUsado;
    }
}
