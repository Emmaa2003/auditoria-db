package com.example.auditoriabd.dto;

import com.example.auditoriabd.entity.Control;

import java.util.List;

public class GrupoControlCuestionario {

    private final Control control;
    private final List<PreguntaIndexada> preguntas;

    public GrupoControlCuestionario(Control control, List<PreguntaIndexada> preguntas) {
        this.control = control;
        this.preguntas = preguntas;
    }

    public Control getControl() {
        return control;
    }

    public List<PreguntaIndexada> getPreguntas() {
        return preguntas;
    }
}
