package com.example.auditoriabd.entity;

public enum PesoControl {
    ALTO(3),
    MEDIO(2),
    BAJO(1);

    private final int valorNumerico;

    PesoControl(int valorNumerico) {
        this.valorNumerico = valorNumerico;
    }

    public int getValorNumerico() {
        return valorNumerico;
    }
}
