package com.livraria.domain;

public class ItemPedido {

    private int quantidade;
    private double precoVenda;
    private Livro livro;

    public ItemPedido(Livro livro, int quantidade, double precoVenda) {
        this.livro = livro;
        this.quantidade = quantidade;
        this.precoVenda = precoVenda;
    }

    public double getSubTotal() {
        return precoVenda * quantidade;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getPrecoVenda() {
        return precoVenda;
    }

    public Livro getLivro() {
        return livro;
    }
}
