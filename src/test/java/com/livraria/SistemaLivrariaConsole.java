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
import com.livraria.domain.ItemCarrinho;
import com.livraria.domain.Livro;
import com.livraria.domain.Pedido;
import com.livraria.domain.StatusLivro;
import com.livraria.domain.StatusPedido;

import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

/**
 * Simulador interativo em console para execucao manual (abordagem caixa-preta)
 * do caso de uso CSU02 - Efetuar Pedido, permitindo informar entradas ao vivo
 * para evidenciar em video todos os cenarios (RN01 a RN05, GFC C1..C7 e GE S1..S3).
 */
public class SistemaLivrariaConsole {

    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        Locale.setDefault(Locale.forLanguageTag("pt-BR"));
        EfetuarPedidoService service = new EfetuarPedidoService();

        System.out.println("====================================================================");
        System.out.println("  E-COMMERCE LIVRARIA - SIMULADOR CAIXA-PRETA (CSU02: EFETUAR PEDIDO)");
        System.out.println("====================================================================");

        boolean continuar = true;
        int numExecucao = 1;
        while (continuar) {
            System.out.println("\n--------------------------------------------------------------------");
            System.out.println("  NOVA EXECUCAO DE TESTE #" + numExecucao++);
            System.out.println("--------------------------------------------------------------------");
            executarFluxoPedido(service);

            System.out.print("\nDeseja executar outro caso de teste? (s/n): ");
            continuar = lerSimNao();
        }

        System.out.println("\nSimulador encerrado.");
    }

    private static void executarFluxoPedido(EfetuarPedidoService service) {
        Carrinho carrinho = new Carrinho();

        // 1. Montagem do Carrinho (RN01 e RN02)
        System.out.print("Deseja adicionar algum livro ao carrinho? (s/n): ");
        while (lerSimNao()) {
            System.out.print("Titulo do livro: ");
            String titulo = lerTextoNaoVazio("Engenharia de Software");

            System.out.print("Formato [1-CapaDura (0% desc) | 2-Brochura (5% desc) | 3-Digital (10% desc)]: ");
            int formato = lerInteiroIntervalo(1, 3);

            System.out.print("Preco base (R$): ");
            double precoBase = lerDoubleMinimo(0.01);

            System.out.print("Status do livro [1-disponivel | 2-indisponivel | 3-foraDeCirculacao]: ");
            int stOpt = lerInteiroIntervalo(1, 3);
            StatusLivro statusLivro = switch (stOpt) {
                case 2 -> StatusLivro.indisponivel;
                case 3 -> StatusLivro.foraDeCirculacao;
                default -> StatusLivro.disponivel;
            };

            System.out.print("Quantidade inicial em estoque: ");
            int qtdEstoque = lerInteiroIntervalo(0, 10000);
            Estoque estoque = new Estoque(qtdEstoque);

            Livro livro = switch (formato) {
                case 2 -> new Brochura(titulo, "978-0002", precoBase, statusLivro, estoque);
                case 3 -> new Digital(titulo, "978-0003", precoBase, statusLivro, estoque);
                default -> new CapaDura(titulo, "978-0001", precoBase, statusLivro, estoque);
            };

            System.out.print("Quantidade desejada para compra: ");
            int qtdCompra = lerInteiroIntervalo(1, 1000);

            try {
                carrinho.adicionarItem(livro, qtdCompra);
                System.out.printf("-> Item adicionado! Preco unitario (RN02): R$ %.2f | Subtotal do carrinho: R$ %.2f%n",
                        livro.getPreco(), carrinho.getTotal());
            } catch (IllegalStateException ex) {
                System.out.println("-> [BLOQUEIO RN01] " + ex.getMessage());
            }

            System.out.print("Deseja adicionar outro livro ao carrinho? (s/n): ");
        }

        // 2. Confirmar carrinho (Passo 1 e 2 do Fluxo Principal)
        System.out.println("\n[Sistema] Validando carrinho para prosseguir com o pedido...");
        try {
            service.visualizarCarrinho(carrinho);
        } catch (IllegalStateException ex) {
            System.out.println("-> [SAIDA DO SISTEMA] " + ex.getMessage());
            return;
        }

        System.out.println("Itens no carrinho:");
        for (ItemCarrinho item : carrinho.getItens()) {
            System.out.printf(" - %s (%s) | Qtd: %d | Unit: R$ %.2f | SubTotal: R$ %.2f%n",
                    item.getLivro().getTitulo(),
                    item.getLivro().getClass().getSimpleName(),
                    item.getQuantidade(),
                    item.calcularPrecoItem(),
                    item.getSubTotal());
        }

        // 3. Cadastro do Cliente e Endereco (Passo 3 / Fluxo Alternativo CSU03)
        System.out.print("\nCliente ja possui cadastro? (s/n): ");
        Cliente cliente;
        Endereco endereco;
        if (lerSimNao()) {
            cliente = new Cliente("Vinicius Lima", "111.222.333-44", "vinicius@fatec.sp.gov.br", "11999990000", new Date());
            System.out.print("Informe o CEP de entrega (ex: 08250-000): ");
            String cep = lerTextoNaoVazio("08250-000");
            endereco = new Endereco(cep, "Av. Aguia de Haia", 2983);
            cliente.getEnderecos().add(endereco);
            System.out.println("-> Cliente autenticado: " + cliente.getNome() + " | CEP: " + endereco.getCep());
        } else {
            System.out.println("[Fluxo Alternativo CSU03 - Cadastro de Cliente]");
            System.out.print("Nome completo: ");
            String nome = lerTextoNaoVazio("Jonatas Palmeira");
            System.out.print("CPF: ");
            String cpf = lerTextoNaoVazio("222.333.444-55");
            System.out.print("E-mail: ");
            String email = lerTextoNaoVazio("jonatas@fatec.sp.gov.br");
            System.out.print("CEP de entrega: ");
            String cep = lerTextoNaoVazio("01001-000");
            System.out.print("Logradouro: ");
            String logradouro = lerTextoNaoVazio("Praca da Se");
            System.out.print("Numero: ");
            int numero = lerInteiroIntervalo(1, 99999);

            cliente = new Cliente(nome, cpf, email, "11988887777", new Date());
            endereco = new Endereco(cep, logradouro, numero);
            cliente.getEnderecos().add(endereco);
            System.out.println("-> Cadastro salvo e cliente autenticado. Retornando ao pedido...");
        }

        // 4. Calculo de Frete (Passo 4)
        System.out.print("\nInforme o valor do frete retornado pelo Sistema de Frete (R$): ");
        double valorFreteInformado = lerDoubleMinimo(0.0);
        CalculadoraFrete apiFrete = cep -> valorFreteInformado;

        double frete = service.calcularFrete(endereco.getCep(), apiFrete);
        double totalBruto = carrinho.getTotal() + frete;
        System.out.printf("-> Frete calculado: R$ %.2f | Total bruto do pedido: R$ %.2f%n", frete, totalBruto);

        // 5. Forma de Pagamento (Passo 5 e RN04)
        Descontavel formaPagamento = null;
        while (formaPagamento == null) {
            System.out.print("\nEscolha a forma de pagamento [1-Pix (8% desc) | 2-Cartao de Credito]: ");
            int opPagto = lerInteiroIntervalo(1, 2);
            if (opPagto == 1) {
                System.out.print("Informe a chave Pix: ");
                String chave = lerTextoNaoVazio("cliente@pix.br");
                formaPagamento = new Pix(chave);
                System.out.printf("-> Pix selecionado (8%% desc RN04). Total a pagar: R$ %.2f%n",
                        formaPagamento.aplicarDesconto(totalBruto));
            } else {
                System.out.print("Informe o numero do cartao: ");
                String numCartao = lerTextoNaoVazio("4111-2222-3333-4444");
                System.out.print("Numero de parcelas (1=a vista 3%% desc | 2 ou 3=sem juros): ");
                int parcelas = lerInteiroIntervalo(1, 12);
                try {
                    formaPagamento = new Cartao(numCartao, parcelas);
                    double totalDesc = formaPagamento.aplicarDesconto(totalBruto);
                    if (parcelas == 1) {
                        System.out.printf("-> Cartao 1x a vista (3%% desc RN04). Total a pagar: R$ %.2f%n", totalDesc);
                    } else {
                        System.out.printf("-> Cartao %dx sem juros (RN04). Total: R$ %.2f (%dx de R$ %.2f)%n",
                                parcelas, totalDesc, parcelas, totalDesc / parcelas);
                    }
                } catch (IllegalArgumentException ex) {
                    System.out.println("-> [BLOQUEIO RN04] " + ex.getMessage());
                }
            }
        }

        // 6. Autorizacao / Processamento (Passo 6 e RN05)
        System.out.print("\nCanal de notificacao ao funcionario (RN03) [1-Email | 2-SMS]: ");
        int opNotif = lerInteiroIntervalo(1, 2);
        Notificavel notificavel = (opNotif == 2)
                ? new SMS("11900001111")
                : new Email("funcionario@livraria.com", "Alerta de Estoque Minimo");

        System.out.print("Simular resposta do Gateway: Pagamento aprovado? (s/n): ");
        boolean aprovarPagto = lerSimNao();
        ProcessadorPagamento apiPagto = (forma, valor) -> aprovarPagto;

        Pedido pedido = service.confirmarPedido(
                formaPagamento, endereco, carrinho, cliente, apiFrete, apiPagto, notificavel);

        System.out.println("\n=== RESULTADO DO PEDIDO ===");
        System.out.println("Numero do Pedido : " + pedido.getNumPedido());
        System.out.println("Status (RN05)    : " + pedido.getStatus());
        System.out.printf("Total Bruto      : R$ %.2f%n", pedido.getValorTotal());
        System.out.printf("Total c/ Desconto: R$ %.2f%n", formaPagamento.aplicarDesconto(pedido.getValorTotal()));
        System.out.println("Itens no Carrinho: " + carrinho.getItens().size());
        for (int i = 0; i < pedido.getItens().size(); i++) {
            Livro l = pedido.getItens().get(i).getLivro();
            System.out.printf("Estoque '%s': %d (estoqueMin? %s)%n",
                    l.getTitulo(), l.getEstoque().getQuantidade(), l.getEstoque().isEstoqueMin());
        }

        // 7. Evolucao de Estados pos-confirmacao (GE S1 / S3)
        if (pedido.getStatus() == StatusPedido.confirmado) {
            System.out.println("\nDeseja simular transicao posterior do pedido (Maquina de Estados)?");
            System.out.print("[1-Avancar ate finalizado (S1) | 2-Cancelar pedido confirmado (S3) | 0-Encerrar]: ");
            int opEstado = lerInteiroIntervalo(0, 2);
            if (opEstado == 1) {
                pedido.setStatus(StatusPedido.emTransporte);
                System.out.println("-> [T6] Status atualizado para: " + pedido.getStatus());
                pedido.setStatus(StatusPedido.finalizado);
                System.out.println("-> [T7] Status atualizado para: " + pedido.getStatus());
            } else if (opEstado == 2) {
                service.cancelarPedido(pedido);
                System.out.println("-> [T5] Status atualizado para: " + pedido.getStatus());
            }
        }
    }

    private static boolean lerSimNao() {
        while (true) {
            String s = sc.nextLine().trim().toLowerCase();
            if (s.equals("s") || s.equals("sim")) return true;
            if (s.equals("n") || s.equals("nao") || s.equals("não")) return false;
            System.out.print("Digite 's' ou 'n': ");
        }
    }

    private static String lerTextoNaoVazio(String padrao) {
        String s = sc.nextLine().trim();
        return s.isEmpty() ? padrao : s;
    }

    private static int lerInteiroIntervalo(int min, int max) {
        while (true) {
            try {
                int v = Integer.parseInt(sc.nextLine().trim());
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) {
            }
            System.out.printf("Digite um numero inteiro entre %d e %d: ", min, max);
        }
    }

    private static double lerDoubleMinimo(double min) {
        while (true) {
            try {
                double v = Double.parseDouble(sc.nextLine().trim().replace(',', '.'));
                if (v >= min) return v;
            } catch (NumberFormatException ignored) {
            }
            System.out.printf("Digite um valor numerico >= %.2f: ", min);
        }
    }
}
