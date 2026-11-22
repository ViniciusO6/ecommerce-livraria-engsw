package com.livraria.application;

import com.livraria.domain.Carrinho;
import com.livraria.domain.Cliente;
import com.livraria.domain.Endereco;
import com.livraria.domain.Estoque;
import com.livraria.domain.ItemCarrinho;
import com.livraria.domain.ItemPedido;
import com.livraria.domain.Pedido;
import com.livraria.domain.StatusPedido;

public class EfetuarPedidoService {

    public Carrinho visualizarCarrinho(Carrinho carrinho) {
        if (carrinho == null || carrinho.getItens().isEmpty()) {
            throw new IllegalStateException("Carrinho vazio. Adicione ao menos um item para prosseguir.");
        }
        return carrinho;
    }

    public double calcularFrete(String cep, CalculadoraFrete apiFrete) {
        if (cep == null || cep.trim().isEmpty()) {
            throw new IllegalArgumentException("CEP de entrega é obrigatório.");
        }
        if (apiFrete == null) {
            throw new IllegalArgumentException("Calculadora de frete não informada.");
        }
        return apiFrete.cotarFrete(cep);
    }

    public Pedido confirmarPedido(
            Descontavel forma,
            Endereco end,
            Carrinho carrinho,
            Cliente cliente,
            CalculadoraFrete apiFrete,
            ProcessadorPagamento apiPagto,
            Notificavel notificavel) {

        visualizarCarrinho(carrinho);
        if (cliente == null || end == null || forma == null || apiPagto == null) {
            throw new IllegalArgumentException("Dados obrigatórios do pedido não informados.");
        }

        for (ItemCarrinho item : carrinho.getItens()) {
            if (!item.validarStatus()) {
                throw new IllegalStateException(
                        "Livro não disponível para compra: " + item.getLivro().getTitulo());
            }
        }

        double valorFrete = calcularFrete(end.getCep(), apiFrete);
        int numPedido = (int) (Math.abs(System.currentTimeMillis()) % Integer.MAX_VALUE);
        Pedido pedido = new Pedido(numPedido, cliente, end, valorFrete);

        for (ItemCarrinho item : carrinho.getItens()) {
            ItemPedido itemPedido = new ItemPedido(
                    item.getLivro(),
                    item.getQuantidade(),
                    item.calcularPrecoItem());
            pedido.getItens().add(itemPedido);
        }

        double totalBruto = pedido.calcularTotal();
        double valorFinal = forma.aplicarDesconto(totalBruto);

        boolean autorizado = forma.autorizar() && apiPagto.processarPagto(forma, valorFinal);
        if (!autorizado) {
            cancelarPedido(pedido);
            return pedido;
        }

        pedido.setStatus(StatusPedido.confirmado);

        for (ItemPedido itemPedido : pedido.getItens()) {
            atualizarEstoque(itemPedido.getLivro().getEstoque(), itemPedido.getQuantidade(), notificavel);
        }

        carrinho.limpar();
        return pedido;
    }

    public void cancelarPedido(Pedido pedido) {
        if (pedido != null) {
            pedido.setStatus(StatusPedido.cancelado);
        }
    }

    public void atualizarEstoque(Estoque estoque, int qtd, Notificavel notificavel) {
        if (estoque == null) {
            throw new IllegalArgumentException("Estoque não informado.");
        }
        estoque.darBaixa(qtd);
        verificarEstoqueMin(estoque, notificavel);
    }

    public void verificarEstoqueMin(Estoque estoque, Notificavel notificavel) {
        if (estoque != null && estoque.isEstoqueMin() && notificavel != null) {
            notificavel.notificar("Alerta RN03: estoque atingiu o nível mínimo (<= 2 exemplares).");
        }
    }
}
