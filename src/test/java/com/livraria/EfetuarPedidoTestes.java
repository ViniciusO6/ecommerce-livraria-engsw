package com.livraria;

import com.livraria.application.CalculadoraFrete;
import com.livraria.application.Cartao;
import com.livraria.application.Descontavel;
import com.livraria.application.EfetuarPedidoService;
import com.livraria.application.Email;
import com.livraria.application.Notificavel;
import com.livraria.application.Pix;
import com.livraria.application.ProcessadorPagamento;
import com.livraria.application.SMS;
import com.livraria.domain.Brochura;
import com.livraria.domain.CapaDura;
import com.livraria.domain.Carrinho;
import com.livraria.domain.Cliente;
import com.livraria.domain.Digital;
import com.livraria.domain.Endereco;
import com.livraria.domain.Estoque;
import com.livraria.domain.Livro;
import com.livraria.domain.Pedido;
import com.livraria.domain.StatusLivro;
import com.livraria.domain.StatusPedido;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Executor de testes funcionais (abordagem caixa-preta) para o caso de uso
 * CSU02 - Efetuar Pedido, cobrindo os caminhos C1..C7 do GFC, as sequências
 * S1..S3 do GE e as regras de negócio RN01..RN05.
 */
public class EfetuarPedidoTestes {

    private static int totalExecutados = 0;
    private static int totalAprovados = 0;

    static class NotificacaoEspiao implements Notificavel {
        final List<String> mensagens = new ArrayList<>();
        private final Notificavel delegate;

        NotificacaoEspiao(Notificavel delegate) {
            this.delegate = delegate;
        }

        @Override
        public void notificar(String msg) {
            mensagens.add(msg);
            delegate.notificar(msg);
        }
    }

    static class PagamentoRegistrador implements ProcessadorPagamento {
        private final boolean aprovar;
        double ultimoValorCobrado = -1.0;

        PagamentoRegistrador(boolean aprovar) {
            this.aprovar = aprovar;
        }

        @Override
        public boolean processarPagto(Descontavel forma, double valor) {
            this.ultimoValorCobrado = valor;
            return aprovar;
        }
    }

    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("  EXECUCAO DOS TESTES DE SISTEMA (CAIXA-PRETA) - CSU02: EFETUAR PEDIDO   ");
        System.out.println("=========================================================================\n");

        executarCT01_CarrinhoVazio_GFC_C1();
        executarCT02_FluxoPrincipalPix_GFC_C2_GE_S1();
        executarCT03_SemCadastroBrochuraPix_GFC_C3();
        executarCT04_CartaoAVistaDigital_GFC_C4();
        executarCT05_CartaoParcelado3x_GFC_C5();
        executarCT06_PagamentoRecusado_GFC_C6_GE_S2();
        executarCT07_EstoqueMinimoNotificacao_GFC_C7();
        executarCT08_CancelamentoPosConfirmacao_GE_S3();
        executarCT09_LivroIndisponivelOuForaCirculacao_RN01();

        System.out.println("=========================================================================");
        System.out.printf("  RESUMO FINAL: %d/%d casos de teste aprovados.%n", totalAprovados, totalExecutados);
        System.out.println("=========================================================================");
        if (totalAprovados != totalExecutados) {
            System.exit(1);
        }
    }

    private static void registrar(String id, String ref, boolean ok, String detalhe) {
        totalExecutados++;
        if (ok) {
            totalAprovados++;
        }
        System.out.printf("[%s] %-4s | %s%n", ok ? "PASSOU" : "FALHOU", id, ref);
        System.out.println("         -> " + detalhe + "\n");
    }

    private static void executarCT01_CarrinhoVazio_GFC_C1() {
        EfetuarPedidoService service = new EfetuarPedidoService();
        Carrinho carrinhoVazio = new Carrinho();
        boolean bloqueou = false;
        String msg = "";
        try {
            service.visualizarCarrinho(carrinhoVazio);
        } catch (IllegalStateException ex) {
            bloqueou = true;
            msg = ex.getMessage();
        }
        registrar("CT01", "GFC C1 (Carrinho vazio)", bloqueou,
                "Entrada: carrinho com 0 itens | Obtido: bloqueio com aviso '" + msg + "'");
    }

    private static void executarCT02_FluxoPrincipalPix_GFC_C2_GE_S1() {
        EfetuarPedidoService service = new EfetuarPedidoService();
        Cliente cliente = new Cliente("Vinicius Lima", "111.222.333-44", "vinicius@fatec.sp.gov.br", "11999990000", new Date());
        Endereco end = new Endereco("08250-000", "Av. Aguia de Haia", 2983);
        cliente.getEnderecos().add(end);

        Estoque estoque = new Estoque(10);
        Livro livro = new CapaDura("Engenharia de Software", "978-0001", 100.00, StatusLivro.disponivel, estoque);
        Carrinho carrinho = new Carrinho();
        carrinho.adicionarItem(livro, 2); // 2 * 100.00 = 200.00 (RN02 CapaDura 0% desc.)

        CalculadoraFrete freteApi = cep -> 20.00; // Total bruto = 220.00
        PagamentoRegistrador pagtoApi = new PagamentoRegistrador(true);
        NotificacaoEspiao notif = new NotificacaoEspiao(new Email("estoque@livraria.com", "Alerta Estoque"));
        Descontavel pix = new Pix("chave-pix-cliente"); // 8% desc. -> 220.00 * 0.92 = 202.40 (RN04)

        Pedido pedido = service.confirmarPedido(pix, end, carrinho, cliente, freteApi, pagtoApi, notif);
        boolean statusConfirmado = pedido.getStatus() == StatusPedido.confirmado;

        // Evolucao GE S1: T6 (emTransporte) -> T7 (finalizado)
        pedido.setStatus(StatusPedido.emTransporte);
        boolean emTransporte = pedido.getStatus() == StatusPedido.emTransporte;
        pedido.setStatus(StatusPedido.finalizado);
        boolean finalizado = pedido.getStatus() == StatusPedido.finalizado;

        boolean ok = statusConfirmado && emTransporte && finalizado
                && Math.abs(pagtoApi.ultimoValorCobrado - 202.40) < 0.01
                && estoque.getQuantidade() == 8
                && carrinho.getItens().isEmpty()
                && notif.mensagens.isEmpty();

        registrar("CT02", "GFC C2 + GE S1 (Pix + CapaDura + Estoque OK)", ok,
                String.format("Bruto: R$ %.2f | Cobrado c/ Pix 8%%: R$ %.2f | Estoque: %d | Carrinho itens: %d | Status final: %s",
                        pedido.getValorTotal(), pagtoApi.ultimoValorCobrado, estoque.getQuantidade(),
                        carrinho.getItens().size(), pedido.getStatus()));
    }

    private static void executarCT03_SemCadastroBrochuraPix_GFC_C3() {
        EfetuarPedidoService service = new EfetuarPedidoService();
        // Fluxo alternativo passo 3: cliente realiza cadastro e cadastra endereco antes de prosseguir
        Cliente novoCliente = new Cliente("Jonatas Palmeira", "222.333.444-55", "jonatas@fatec.sp.gov.br", "11988887777", new Date());
        Endereco end = new Endereco("01001-000", "Praca da Se", 100);
        novoCliente.getEnderecos().add(end);

        Estoque estoque = new Estoque(6);
        Livro livro = new Brochura("Clean Code", "978-0002", 100.00, StatusLivro.disponivel, estoque); // 5% desc = 95.00
        Carrinho carrinho = new Carrinho();
        carrinho.adicionarItem(livro, 1);

        CalculadoraFrete freteApi = cep -> 15.00; // Bruto = 95 + 15 = 110.00
        PagamentoRegistrador pagtoApi = new PagamentoRegistrador(true);
        Notificavel notif = new Email("estoque@livraria.com", "Alerta Estoque");
        Descontavel pix = new Pix("jonatas@pix.br"); // 8% desc -> 110 * 0.92 = 101.20

        Pedido pedido = service.confirmarPedido(pix, end, carrinho, novoCliente, freteApi, pagtoApi, notif);
        boolean ok = pedido.getStatus() == StatusPedido.confirmado
                && Math.abs(livro.getPreco() - 95.00) < 0.01
                && Math.abs(pagtoApi.ultimoValorCobrado - 101.20) < 0.01
                && estoque.getQuantidade() == 5;

        registrar("CT03", "GFC C3 (Fluxo Alternativo CSU03 + Brochura 5% + Pix 8%)", ok,
                String.format("Livro Brochura: R$ %.2f | Cobrado Pix: R$ %.2f | Status: %s | Estoque: %d",
                        livro.getPreco(), pagtoApi.ultimoValorCobrado, pedido.getStatus(), estoque.getQuantidade()));
    }

    private static void executarCT04_CartaoAVistaDigital_GFC_C4() {
        EfetuarPedidoService service = new EfetuarPedidoService();
        Cliente cliente = new Cliente("Maria Silva", "333.444.555-66", "maria@email.com", "11977776666", new Date());
        Endereco end = new Endereco("04567-000", "Av. Paulista", 1000);
        cliente.getEnderecos().add(end);

        Estoque estoque = new Estoque(10);
        Livro livro = new Digital("Padroes de Projeto", "978-0003", 100.00, StatusLivro.disponivel, estoque); // 10% desc = 90.00
        Carrinho carrinho = new Carrinho();
        carrinho.adicionarItem(livro, 1);

        CalculadoraFrete freteApi = cep -> 0.00; // Bruto = 90.00
        PagamentoRegistrador pagtoApi = new PagamentoRegistrador(true);
        Notificavel notif = new SMS("11999998888");
        Descontavel cartao1x = new Cartao("4111-2222-3333-4444", 1); // 3% desc -> 90 * 0.97 = 87.30

        Pedido pedido = service.confirmarPedido(cartao1x, end, carrinho, cliente, freteApi, pagtoApi, notif);
        boolean ok = pedido.getStatus() == StatusPedido.confirmado
                && Math.abs(livro.getPreco() - 90.00) < 0.01
                && Math.abs(pagtoApi.ultimoValorCobrado - 87.30) < 0.01;

        registrar("CT04", "GFC C4 (Cartao 1x a vista 3% + Digital 10%)", ok,
                String.format("Livro Digital: R$ %.2f | Cobrado Cartao 1x (3%% desc): R$ %.2f | Status: %s",
                        livro.getPreco(), pagtoApi.ultimoValorCobrado, pedido.getStatus()));
    }

    private static void executarCT05_CartaoParcelado3x_GFC_C5() {
        EfetuarPedidoService service = new EfetuarPedidoService();
        Cliente cliente = new Cliente("Carlos Souza", "444.555.666-77", "carlos@email.com", "11966665555", new Date());
        Endereco end = new Endereco("03000-000", "Rua Tuiuti", 500);
        cliente.getEnderecos().add(end);

        Estoque estoque = new Estoque(8);
        Livro livro = new CapaDura("Arquitetura Limpa", "978-0004", 120.00, StatusLivro.disponivel, estoque);
        Carrinho carrinho = new Carrinho();
        carrinho.adicionarItem(livro, 1);

        CalculadoraFrete freteApi = cep -> 30.00; // Bruto = 150.00
        PagamentoRegistrador pagtoApi = new PagamentoRegistrador(true);
        Notificavel notif = new Email("estoque@livraria.com", "Alerta");
        Descontavel cartao3x = new Cartao("5555-6666-7777-8888", 3); // sem juros, sem desconto = 150.00

        Pedido pedido = service.confirmarPedido(cartao3x, end, carrinho, cliente, freteApi, pagtoApi, notif);

        boolean bloqueou4Parcelas = false;
        try {
            new Cartao("5555-6666-7777-8888", 4);
        } catch (IllegalArgumentException ex) {
            bloqueou4Parcelas = true;
        }

        boolean ok = pedido.getStatus() == StatusPedido.confirmado
                && Math.abs(pagtoApi.ultimoValorCobrado - 150.00) < 0.01
                && bloqueou4Parcelas;

        registrar("CT05", "GFC C5 (Cartao parcelado 3x sem juros + limite RN04)", ok,
                String.format("Cobrado 3x: R$ %.2f | Bloqueio 4x: %s | Status: %s",
                        pagtoApi.ultimoValorCobrado, bloqueou4Parcelas, pedido.getStatus()));
    }

    private static void executarCT06_PagamentoRecusado_GFC_C6_GE_S2() {
        EfetuarPedidoService service = new EfetuarPedidoService();
        Cliente cliente = new Cliente("Ana Costa", "555.666.777-88", "ana@email.com", "11955554444", new Date());
        Endereco end = new Endereco("02000-000", "Av. Cruzeiro do Sul", 200);
        cliente.getEnderecos().add(end);

        Estoque estoque = new Estoque(5);
        Livro livro = new CapaDura("Domain-Driven Design", "978-0005", 150.00, StatusLivro.disponivel, estoque);
        Carrinho carrinho = new Carrinho();
        carrinho.adicionarItem(livro, 1);

        CalculadoraFrete freteApi = cep -> 20.00;
        PagamentoRegistrador pagtoRecusado = new PagamentoRegistrador(false);
        Notificavel notif = new SMS("11955554444");
        Descontavel pix = new Pix("ana@pix.com");

        Pedido pedido = service.confirmarPedido(pix, end, carrinho, cliente, freteApi, pagtoRecusado, notif);

        boolean ok = pedido.getStatus() == StatusPedido.cancelado
                && estoque.getQuantidade() == 5
                && carrinho.getItens().size() == 1;

        registrar("CT06", "GFC C6 + GE S2 (Pagamento recusado -> cancelado)", ok,
                String.format("Status: %s | Estoque mantido: %d | Itens mantidos no carrinho: %d",
                        pedido.getStatus(), estoque.getQuantidade(), carrinho.getItens().size()));
    }

    private static void executarCT07_EstoqueMinimoNotificacao_GFC_C7() {
        EfetuarPedidoService service = new EfetuarPedidoService();
        Cliente cliente = new Cliente("Pedro Alves", "666.777.888-99", "pedro@email.com", "11944443333", new Date());
        Endereco end = new Endereco("05000-000", "Rua Cerro Cora", 80);
        cliente.getEnderecos().add(end);

        Estoque estoque = new Estoque(3); // Comprando 1 -> cai para 2 (<= estoqueMin) -> RN03
        Livro livro = new CapaDura("Testes Automatizados", "978-0006", 80.00, StatusLivro.disponivel, estoque);
        Carrinho carrinho = new Carrinho();
        carrinho.adicionarItem(livro, 1);

        CalculadoraFrete freteApi = cep -> 10.00;
        PagamentoRegistrador pagtoApi = new PagamentoRegistrador(true);
        NotificacaoEspiao notif = new NotificacaoEspiao(new SMS("11900001111"));
        Descontavel pix = new Pix("pedro@pix.com");

        Pedido pedido = service.confirmarPedido(pix, end, carrinho, cliente, freteApi, pagtoApi, notif);

        boolean ok = pedido.getStatus() == StatusPedido.confirmado
                && estoque.getQuantidade() == 2
                && estoque.isEstoqueMin()
                && notif.mensagens.size() == 1;

        registrar("CT07", "GFC C7 (Estoque minimo <= 2 -> Notificacao RN03)", ok,
                String.format("Estoque apos baixa: %d | Notificacoes enviadas: %d",
                        estoque.getQuantidade(), notif.mensagens.size()));
    }

    private static void executarCT08_CancelamentoPosConfirmacao_GE_S3() {
        EfetuarPedidoService service = new EfetuarPedidoService();
        Cliente cliente = new Cliente("Lucas Dias", "777.888.999-00", "lucas@email.com", "11933332222", new Date());
        Endereco end = new Endereco("06000-000", "Av. dos Autonomistas", 150);
        Estoque estoque = new Estoque(10);
        Livro livro = new CapaDura("Java Efetivo", "978-0007", 110.00, StatusLivro.disponivel, estoque);
        Carrinho carrinho = new Carrinho();
        carrinho.adicionarItem(livro, 1);

        Pedido pedido = service.confirmarPedido(
                new Pix("lucas@pix.com"), end, carrinho, cliente,
                cep -> 15.00, new PagamentoRegistrador(true), new Email("estoque@livraria.com", "Alerta"));

        boolean confirmadoAntes = pedido.getStatus() == StatusPedido.confirmado;
        service.cancelarPedido(pedido);
        boolean canceladoDepois = pedido.getStatus() == StatusPedido.cancelado;

        registrar("CT08", "GE S3 (confirmado -> T5 cancelarPedido -> cancelado)",
                confirmadoAntes && canceladoDepois,
                "Transicao: " + (confirmadoAntes ? "confirmado" : "erro") + " -> " + pedido.getStatus());
    }

    private static void executarCT09_LivroIndisponivelOuForaCirculacao_RN01() {
        Estoque estoque = new Estoque(0);
        Livro livroIndisp = new CapaDura("Livro Esgotado", "978-0008", 90.00, StatusLivro.indisponivel, estoque);
        Livro livroFora = new Brochura("Livro Descontinuado", "978-0009", 90.00, StatusLivro.foraDeCirculacao, estoque);
        Carrinho carrinho = new Carrinho();

        boolean bloqIndisp = false;
        boolean bloqFora = false;
        try {
            carrinho.adicionarItem(livroIndisp, 1);
        } catch (IllegalStateException ex) {
            bloqIndisp = true;
        }
        try {
            carrinho.adicionarItem(livroFora, 1);
        } catch (IllegalStateException ex) {
            bloqFora = true;
        }

        registrar("CT09", "RN01 (Livro indisponivel / foraDeCirculacao)",
                bloqIndisp && bloqFora && carrinho.getItens().isEmpty(),
                "Bloqueio indisponivel: " + bloqIndisp + " | Bloqueio foraDeCirculacao: " + bloqFora);
    }
}
