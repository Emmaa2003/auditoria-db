package com.example.auditoriabd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(name = "resultado_control", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_auditoria", "id_control"})
})
public class ResultadoControl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resultado_control")
    private Integer idResultadoControl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_auditoria", nullable = false)
    private Auditoria auditoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_control", nullable = false)
    private Control control;

    @Column(name = "porcentaje_cumplimiento", precision = 5, scale = 2)
    private BigDecimal porcentajeCumplimiento;

    @Column(name = "nivel_madurez")
    private Short nivelMadurez;

    @Column(name = "riesgo_control", precision = 5, scale = 2)
    private BigDecimal riesgoControl;

    public Integer getIdResultadoControl() {
        return idResultadoControl;
    }

    public void setIdResultadoControl(Integer idResultadoControl) {
        this.idResultadoControl = idResultadoControl;
    }

    public Auditoria getAuditoria() {
        return auditoria;
    }

    public void setAuditoria(Auditoria auditoria) {
        this.auditoria = auditoria;
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public BigDecimal getPorcentajeCumplimiento() {
        return porcentajeCumplimiento;
    }

    public void setPorcentajeCumplimiento(BigDecimal porcentajeCumplimiento) {
        this.porcentajeCumplimiento = porcentajeCumplimiento;
    }

    public Short getNivelMadurez() {
        return nivelMadurez;
    }

    public void setNivelMadurez(Short nivelMadurez) {
        this.nivelMadurez = nivelMadurez;
    }

    public BigDecimal getRiesgoControl() {
        return riesgoControl;
    }

    public void setRiesgoControl(BigDecimal riesgoControl) {
        this.riesgoControl = riesgoControl;
    }
}
