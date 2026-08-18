package com.example.auditoriabd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Integer idAuditoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_organizacion", nullable = false)
    private Organizacion organizacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_auditor", nullable = false)
    private Usuario auditor;

    @Column(name = "fecha_auditoria", nullable = false)
    private LocalDate fechaAuditoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoAuditoria estado = EstadoAuditoria.EN_PROGRESO;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void alCrear() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    public Integer getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Integer idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public Organizacion getOrganizacion() {
        return organizacion;
    }

    public void setOrganizacion(Organizacion organizacion) {
        this.organizacion = organizacion;
    }

    public Usuario getAuditor() {
        return auditor;
    }

    public void setAuditor(Usuario auditor) {
        this.auditor = auditor;
    }

    public LocalDate getFechaAuditoria() {
        return fechaAuditoria;
    }

    public void setFechaAuditoria(LocalDate fechaAuditoria) {
        this.fechaAuditoria = fechaAuditoria;
    }

    public EstadoAuditoria getEstado() {
        return estado;
    }

    public void setEstado(EstadoAuditoria estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
