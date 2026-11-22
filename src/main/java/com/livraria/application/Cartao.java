package com.livraria.application;

public class Cartao implements Descontavel {

    private String numCartao;
    private int parcelas;

    public Cartao(String numCartao, int parcelas) {
        if (parcelas < 1 || parcelas > 3) {
            throw new IllegalArgumentException("Parcelamento no cartão permitido apenas de 1 a 3 vezes sem juros.");
        }
        this.numCartao = numCartao;
        this.parcelas = parcelas;
    }

    @Override
    public double aplicarDesconto(double valorTotal) {
        if (parcelas == 1) {
            return valorTotal * 0.97;
        }
        return valorTotal;
    }

    @Override
    public boolean autorizar() {
        return numCartao != null && !numCartao.trim().isEmpty() && parcelas >= 1 && parcelas <= 3;
    }

    public String getNumCartao() {
        return numCartao;
    }

    public int getParcelas() {
        return parcelas;
    }
}
