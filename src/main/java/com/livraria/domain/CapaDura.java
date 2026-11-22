package com.livraria.domain;

public class CapaDura extends Livro {

    public CapaDura(String titulo, String isbn, double precoBase, StatusLivro status, Estoque estoque) {
        super(titulo, isbn, precoBase, status, estoque);
    }

    @Override
    public double getPreco() {
        return precoBase;
    }
}
