package com.livraria.domain;

import java.util.ArrayList;
import java.util.List;

public class Carrinho {

    private double total;
    private List<ItemCarrinho> itens;

    public Carrinho() {
        this.total = 0.0;
        this.itens = new ArrayList<>();
    }

    public void adicionarItem(Livro livro, int qtd) {
        ItemCarrinho item = new ItemCarrinho(livro, qtd);
        if (!item.validarStatus()) {
            throw new IllegalStateException("Livro indisponível ou fora de circulação.");
        }
        this.itens.add(item);
        this.total += item.getSubTotal();
    }

    public void limpar() {
        this.itens.clear();
        this.total = 0.0;
    }

    public double getTotal() {
        return total;
    }

    public List<ItemCarrinho> getItens() {
        return itens;
    }
}
