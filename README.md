# E-commerce de Livraria — CSU02: Efetuar Pedido

Implementação em Java das classes de **Aplicação** e **Domínio** para o caso de uso **Efetuar Pedido (CSU02)**, desenvolvida para o Trabalho 4 da disciplina de **Engenharia de Software** (ADS – AMS, FATEC Zona Leste, 2026/2, Prof. Wilson Vendramel).

## Arquitetura do Projeto

O código está organizado em duas camadas bem delimitadas sob `src/main/java`:

- **`com.livraria.application`**:
  - Serviço orquestrador: `EfetuarPedidoService` (`visualizarCarrinho`, `calcularFrete`, `confirmarPedido`, `cancelarPedido`, `atualizarEstoque`, `verificarEstoqueMin`) sem atributos de instância.
  - Interfaces desacopladas: `CalculadoraFrete`, `ProcessadorPagamento`, `Descontavel`, `Notificavel`.
  - Implementações de pagamento e notificação: `Cartao`, `Pix`, `Email`, `SMS`.
- **`com.livraria.domain`**:
  - Entidades: `Pedido`, `ItemPedido`, `Carrinho`, `ItemCarrinho`, `Estoque`, `Cliente`, `Endereco`.
  - Polimorfismo de formato de livro (RN02): classe abstrata `Livro` e subclasses `CapaDura` (0% desc.), `Brochura` (5% desc.) e `Digital` (10% desc.).
  - Enums de estado: `StatusLivro` (`disponivel`, `indisponivel`, `foraDeCirculacao`) e `StatusPedido` (`emProcessamento`, `cancelado`, `confirmado`, `emTransporte`, `finalizado`).

## Regras de Negócio Atendidas

- **RN01:** Controle de status do livro (`disponivel`, `indisponivel`, `foraDeCirculacao`).
- **RN02:** Cálculo polimórfico de preço por formato (`CapaDura` sem desconto, `Brochura` 5%, `Digital` 10%).
- **RN03:** Estoque mínimo de `2` exemplares com notificação ao funcionário via `Notificavel` (`Email` / `SMS`).
- **RN04:** Pagamento por `Cartao` (até 3x sem juros ou 3% à vista) ou `Pix` (8% à vista).
- **RN05:** Controle do ciclo de vida dos estados de `Pedido` (`emProcessamento`, `cancelado`, `confirmado`, `emTransporte`, `finalizado`).

## Como Compilar e Executar os Testes Caixa-Preta

No diretório raiz do repositório (JDK 17+):

### Windows (PowerShell)
```powershell
New-Item -ItemType Directory -Force -Path out | Out-Null
$files = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $files
java -cp out com.livraria.EfetuarPedidoTestes
# Ou para executar o simulador interativo (caixa-preta ao vivo):
java -cp out com.livraria.SistemaLivrariaConsole
```

### Linux / macOS
```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out com.livraria.EfetuarPedidoTestes
# Ou simulador interativo:
java -cp out com.livraria.SistemaLivrariaConsole
```
