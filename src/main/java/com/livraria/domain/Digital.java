package com.livraria.domain;

public class Digital extends Livro {

    public Digital(String titulo, String isbn, double precoBase, StatusLivro status, Estoque estoque) {
        super(titulo, isbn, precoBase, status, estoque);
    }

    @Override
    public double getPreco() {
        return precoBase * 0.90;
    }
}
