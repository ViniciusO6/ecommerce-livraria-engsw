package com.livraria.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Cliente {

    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private Date dataNasc;
    private List<Endereco> enderecos;

    public Cliente(String nome, String cpf, String email, String telefone, Date dataNasc) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.dataNasc = dataNasc;
        this.enderecos = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public Date getDataNasc() {
        return dataNasc;
    }

    public List<Endereco> getEnderecos() {
        return enderecos;
    }
}
