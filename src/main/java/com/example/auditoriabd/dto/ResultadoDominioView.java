package com.example.auditoriabd.dto;

import java.math.BigDecimal;

public class ResultadoDominioView {
    private String dominio;
    private BigDecimal madurezPromedio;

    public ResultadoDominioView(String dominio, BigDecimal madurezPromedio) {
        this.dominio = dominio;
        this.madurezPromedio = madurezPromedio;
    }

    public String getDominio() {
        return dominio;
    }

    public BigDecimal getMadurezPromedio() {
        return madurezPromedio;
    }
}
