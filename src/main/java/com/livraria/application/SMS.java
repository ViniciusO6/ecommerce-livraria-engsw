package com.livraria.application;

public class SMS implements Notificavel {

    private String telefoneDestino;

    public SMS(String telefoneDestino) {
        this.telefoneDestino = telefoneDestino;
    }

    @Override
    public void notificar(String msg) {
        System.out.println("[SMS para " + telefoneDestino + "]: " + msg);
    }

    public String getTelefoneDestino() {
        return telefoneDestino;
    }
}
