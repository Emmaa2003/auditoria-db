package com.example.auditoriabd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "resultado_auditoria")
public class ResultadoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resultado_auditoria")
    private Integer idResultadoAuditoria;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_auditoria", nullable = false, unique = true)
    private Auditoria auditoria;

    @Column(name = "madurez_general", precision = 4, scale = 2)
    private BigDecimal madurezGeneral;

    @Column(name = "riesgo_confidencialidad", precision = 5, scale = 2)
    private BigDecimal riesgoConfidencialidad;

    @Column(name = "riesgo_integridad", precision = 5, scale = 2)
    private BigDecimal riesgoIntegridad;

    @Column(name = "riesgo_disponibilidad", precision = 5, scale = 2)
    private BigDecimal riesgoDisponibilidad;

    @Column(name = "indice_riesgo_general", precision = 5, scale = 2)
    private BigDecimal indiceRiesgoGeneral;

    @Column(name = "fecha_calculo", nullable = false, updatable = false, insertable = false)
    private LocalDateTime fechaCalculo;

    public Integer getIdResultadoAuditoria() {
        return idResultadoAuditoria;
    }

    public void setIdResultadoAuditoria(Integer idResultadoAuditoria) {
        this.idResultadoAuditoria = idResultadoAuditoria;
    }

    public Auditoria getAuditoria() {
        return auditoria;
    }

    public void setAuditoria(Auditoria auditoria) {
        this.auditoria = auditoria;
    }

    public BigDecimal getMadurezGeneral() {
        return madurezGeneral;
    }

    public void setMadurezGeneral(BigDecimal madurezGeneral) {
        this.madurezGeneral = madurezGeneral;
    }

    public BigDecimal getRiesgoConfidencialidad() {
        return riesgoConfidencialidad;
    }

    public void setRiesgoConfidencialidad(BigDecimal riesgoConfidencialidad) {
        this.riesgoConfidencialidad = riesgoConfidencialidad;
    }

    public BigDecimal getRiesgoIntegridad() {
        return riesgoIntegridad;
    }

    public void setRiesgoIntegridad(BigDecimal riesgoIntegridad) {
        this.riesgoIntegridad = riesgoIntegridad;
    }

    public BigDecimal getRiesgoDisponibilidad() {
        return riesgoDisponibilidad;
    }

    public void setRiesgoDisponibilidad(BigDecimal riesgoDisponibilidad) {
        this.riesgoDisponibilidad = riesgoDisponibilidad;
    }

    public BigDecimal getIndiceRiesgoGeneral() {
        return indiceRiesgoGeneral;
    }

    public void setIndiceRiesgoGeneral(BigDecimal indiceRiesgoGeneral) {
        this.indiceRiesgoGeneral = indiceRiesgoGeneral;
    }

    public LocalDateTime getFechaCalculo() {
        return fechaCalculo;
    }
}
