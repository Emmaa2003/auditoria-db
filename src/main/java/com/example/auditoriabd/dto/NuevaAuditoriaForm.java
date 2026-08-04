package com.example.auditoriabd.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class NuevaAuditoriaForm {

    @NotNull(message = "Debe seleccionar una organización")
    private Integer idOrganizacion;

    @NotNull(message = "Debe indicar la fecha de la auditoría")
    private LocalDate fechaAuditoria;

    public Integer getIdOrganizacion() {
        return idOrganizacion;
    }

    public void setIdOrganizacion(Integer idOrganizacion) {
        this.idOrganizacion = idOrganizacion;
    }

    public LocalDate getFechaAuditoria() {
        return fechaAuditoria;
    }

    public void setFechaAuditoria(LocalDate fechaAuditoria) {
        this.fechaAuditoria = fechaAuditoria;
    }
}
