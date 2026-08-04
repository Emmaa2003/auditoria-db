package com.example.auditoriabd.dto;

import com.example.auditoriabd.entity.Pregunta;

public class PreguntaIndexada {

    private final Pregunta pregunta;
    private final int indice;

    public PreguntaIndexada(Pregunta pregunta, int indice) {
        this.pregunta = pregunta;
        this.indice = indice;
    }

    public Pregunta getPregunta() {
        return pregunta;
    }

    public int getIndice() {
        return indice;
    }
}
