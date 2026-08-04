package com.example.auditoriabd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OrganizacionForm {

    private Integer idOrganizacion;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    private String nombre;

    @NotBlank(message = "El área evaluada es obligatoria")
    @Size(max = 150, message = "El área evaluada no puede superar los 150 caracteres")
    private String areaEvaluada;

    @Size(max = 150, message = "El administrador de BD no puede superar los 150 caracteres")
    private String administradorBd;

    public Integer getIdOrganizacion() {
        return idOrganizacion;
    }

    public void setIdOrganizacion(Integer idOrganizacion) {
        this.idOrganizacion = idOrganizacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAreaEvaluada() {
        return areaEvaluada;
    }

    public void setAreaEvaluada(String areaEvaluada) {
        this.areaEvaluada = areaEvaluada;
    }

    public String getAdministradorBd() {
        return administradorBd;
    }

    public void setAdministradorBd(String administradorBd) {
        this.administradorBd = administradorBd;
    }
}
