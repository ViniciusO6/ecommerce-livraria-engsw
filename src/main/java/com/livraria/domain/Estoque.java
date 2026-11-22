package com.livraria.domain;

public class Estoque {

    private int quantidade;
    private int estoqueMin = 2;

    public Estoque(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade em estoque não pode ser negativa.");
        }
        this.quantidade = quantidade;
    }

    public void darBaixa(int qtd) {
        if (qtd <= 0) {
            throw new IllegalArgumentException("Quantidade de baixa deve ser maior que zero.");
        }
        if (qtd > this.quantidade) {
            throw new IllegalStateException("Estoque insuficiente para baixa.");
        }
        this.quantidade -= qtd;
    }

    public boolean isEstoqueMin() {
        return this.quantidade <= this.estoqueMin;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public int getEstoqueMin() {
        return estoqueMin;
    }
}
