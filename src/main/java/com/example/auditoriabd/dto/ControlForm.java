package com.example.auditoriabd.dto;

import com.example.auditoriabd.entity.PesoControl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ControlForm {

    private Integer idControl;
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150)
    private String nombre;

    @Size(max = 300)
    private String objetivo;

    private String descripcion;

    @NotNull(message = "El peso es obligatorio")
    private PesoControl peso;

    private boolean afectaC;
    private boolean afectaI;
    private boolean afectaD;

    public Integer getIdControl() {
        return idControl;
    }

    public void setIdControl(Integer idControl) {
        this.idControl = idControl;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public PesoControl getPeso() {
        return peso;
    }

    public void setPeso(PesoControl peso) {
        this.peso = peso;
    }

    public boolean isAfectaC() {
        return afectaC;
    }

    public void setAfectaC(boolean afectaC) {
        this.afectaC = afectaC;
    }

    public boolean isAfectaI() {
        return afectaI;
    }

    public void setAfectaI(boolean afectaI) {
        this.afectaI = afectaI;
    }

    public boolean isAfectaD() {
        return afectaD;
    }

    public void setAfectaD(boolean afectaD) {
        this.afectaD = afectaD;
    }
}
