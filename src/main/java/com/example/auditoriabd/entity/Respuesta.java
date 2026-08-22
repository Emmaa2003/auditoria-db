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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "respuesta", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_auditoria", "id_pregunta"})
})
public class Respuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respuesta")
    private Integer idRespuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_auditoria", nullable = false)
    private Auditoria auditoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private Pregunta pregunta;

    @Enumerated(EnumType.STRING)
    @Column(name = "valor", nullable = false, length = 10)
    private ValorRespuesta valor;

    @Lob
    @Column(name = "observacion")
    private String observacion;

    @Column(name = "evidencia", length = 300)
    private String evidencia;

    public Integer getIdRespuesta() {
        return idRespuesta;
    }

    public void setIdRespuesta(Integer idRespuesta) {
        this.idRespuesta = idRespuesta;
    }

    public Auditoria getAuditoria() {
        return auditoria;
    }

    public void setAuditoria(Auditoria auditoria) {
        this.auditoria = auditoria;
    }

    public Pregunta getPregunta() {
        return pregunta;
    }

    public void setPregunta(Pregunta pregunta) {
        this.pregunta = pregunta;
    }

    public ValorRespuesta getValor() {
        return valor;
    }

    public void setValor(ValorRespuesta valor) {
        this.valor = valor;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getEvidencia() {
        return evidencia;
    }

    public void setEvidencia(String evidencia) {
        this.evidencia = evidencia;
    }
}
