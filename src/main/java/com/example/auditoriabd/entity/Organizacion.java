package com.example.auditoriabd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "organizacion")
public class Organizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_organizacion")
    private Integer idOrganizacion;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "area_evaluada", nullable = false, length = 150)
    private String areaEvaluada;

    @Column(name = "administrador_bd", length = 150)
    private String administradorBd;

    @Column(name = "fecha_registro", nullable = false, updatable = false, insertable = false)
    private LocalDateTime fechaRegistro;

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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}
