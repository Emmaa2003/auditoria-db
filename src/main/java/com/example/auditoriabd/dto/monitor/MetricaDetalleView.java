package com.example.auditoriabd.dto.monitor;

/**
 * Una metrica cruda de utilizacion/presion (ej. "Uso de procesos: 18% (Normal)")
 * mostrada dentro de la tarjeta de un componente del monitor.
 */
public class MetricaDetalleView {

    private final String nombre;
    private final String valorTexto;
    private final String nivelTexto;
    private final String nivelCss;

    public MetricaDetalleView(String nombre, String valorTexto, String nivelTexto, String nivelCss) {
        this.nombre = nombre;
        this.valorTexto = valorTexto;
        this.nivelTexto = nivelTexto;
        this.nivelCss = nivelCss;
    }

    public String getNombre() {
        return nombre;
    }

    public String getValorTexto() {
        return valorTexto;
    }

    public String getNivelTexto() {
        return nivelTexto;
    }

    public String getNivelCss() {
        return nivelCss;
    }
}
