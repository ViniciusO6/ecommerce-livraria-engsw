package com.livraria.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Pedido {

    private int numPedido;
    private Date dataPedido;
    private double valorTotal;
    private double valorFrete;
    private StatusPedido status;
    private Cliente cliente;
    private Endereco endereco;
    private List<ItemPedido> itens;

    public Pedido(int numPedido, Cliente cliente, Endereco endereco, double valorFrete) {
        this.numPedido = numPedido;
        this.dataPedido = new Date();
        this.cliente = cliente;
        this.endereco = endereco;
        this.valorFrete = valorFrete;
        this.status = StatusPedido.emProcessamento;
        this.itens = new ArrayList<>();
        this.valorTotal = valorFrete;
    }

    public double calcularTotal() {
        double somaItens = 0.0;
        for (ItemPedido item : itens) {
            somaItens += item.getSubTotal();
        }
        this.valorTotal = somaItens + this.valorFrete;
        return this.valorTotal;
    }

    public int getNumPedido() {
        return numPedido;
    }

    public Date getDataPedido() {
        return dataPedido;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public double getValorFrete() {
        return valorFrete;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }
}
