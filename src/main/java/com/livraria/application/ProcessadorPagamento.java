package com.livraria.application;

public interface ProcessadorPagamento {

    boolean processarPagto(Descontavel forma, double valor);
}
