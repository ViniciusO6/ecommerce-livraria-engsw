package com.livraria.domain;

public abstract class Livro {

    private String titulo;
    private String isbn;
    protected double precoBase;
    private StatusLivro status;
    private Estoque estoque;

    public Livro(String titulo, String isbn, double precoBase, StatusLivro status, Estoque estoque) {
        this.titulo = titulo;
        this.isbn = isbn;
        this.precoBase = precoBase;
        this.status = status;
        this.estoque = estoque;
    }

    public abstract double getPreco();

    public String getTitulo() {
        return titulo;
    }

    public String getIsbn() {
        return isbn;
    }

    public StatusLivro getStatus() {
        return status;
    }

    public Estoque getEstoque() {
        return estoque;
    }
}
