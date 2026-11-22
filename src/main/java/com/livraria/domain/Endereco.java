package com.livraria.domain;

public class Endereco {

    private String cep;
    private String logradouro;
    private int numero;

    public Endereco(String cep, String logradouro, int numero) {
        this.cep = cep;
        this.logradouro = logradouro;
        this.numero = numero;
    }

    public String getCep() {
        return cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public int getNumero() {
        return numero;
    }
}
