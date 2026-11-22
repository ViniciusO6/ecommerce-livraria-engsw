package com.livraria.application;

public class Email implements Notificavel {

    private String emailDestino;
    private String assunto;

    public Email(String emailDestino, String assunto) {
        this.emailDestino = emailDestino;
        this.assunto = assunto;
    }

    @Override
    public void notificar(String msg) {
        System.out.println("[EMAIL para " + emailDestino + " | " + assunto + "]: " + msg);
    }

    public String getEmailDestino() {
        return emailDestino;
    }

    public String getAssunto() {
        return assunto;
    }
}
