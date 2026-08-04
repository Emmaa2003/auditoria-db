package com.example.auditoriabd.dto;

import java.util.ArrayList;
import java.util.List;

public class CuestionarioForm {

    private List<RespuestaForm> respuestas = new ArrayList<>();

    public List<RespuestaForm> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(List<RespuestaForm> respuestas) {
        this.respuestas = respuestas;
    }
}
