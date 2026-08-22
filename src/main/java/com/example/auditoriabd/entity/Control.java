package com.example.auditoriabd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "control")
public class Control {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_control")
    private Integer idControl;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "dominio", nullable = false, length = 50)
    private String dominio;

    @Column(name = "objetivo", length = 300)
    private String objetivo;

    @Lob
    @Column(name = "descripcion")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "peso", nullable = false, length = 10)
    private PesoControl peso;

    @Column(name = "afecta_c", nullable = false)
    private boolean afectaC;

    @Column(name = "afecta_i", nullable = false)
    private boolean afectaI;

    @Column(name = "afecta_d", nullable = false)
    private boolean afectaD;

    @OneToMany(mappedBy = "control", fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    private List<Pregunta> preguntas;

    public List<Pregunta> getPreguntas() {
        return preguntas;
    }

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

    public String getDominio() {
        return dominio;
    }

    public void setDominio(String dominio) {
        this.dominio = dominio;
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
