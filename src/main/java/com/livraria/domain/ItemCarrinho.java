package com.livraria.domain;

public class ItemCarrinho {

    private int quantidade;
    private Livro livro;

    public ItemCarrinho(Livro livro, int quantidade) {
        if (livro == null) {
            throw new IllegalArgumentException("Livro não pode ser nulo.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        this.livro = livro;
        this.quantidade = quantidade;
    }

    public double getSubTotal() {
        return calcularPrecoItem() * quantidade;
    }

    public boolean validarStatus() {
        return livro != null && livro.getStatus() == StatusLivro.disponivel;
    }

    public double calcularPrecoItem() {
        return livro.getPreco();
    }

    public int getQuantidade() {
        return quantidade;
    }

    public Livro getLivro() {
        return livro;
    }
}
