package com.livraria.application;

public class Pix implements Descontavel {

    private String chavePix;

    public Pix(String chavePix) {
        this.chavePix = chavePix;
    }

    @Override
    public double aplicarDesconto(double valorTotal) {
        return valorTotal * 0.92;
    }

    @Override
    public boolean autorizar() {
        return chavePix != null && !chavePix.trim().isEmpty();
    }

    public String getChavePix() {
        return chavePix;
    }
}
