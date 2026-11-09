# Montanha de Gastos — Back-end (Parte 1)

API Spring Boot (Java 21) que calcula o custo acumulado de um hábito recorrente ao longo do tempo e quanto esse mesmo dinheiro renderia em termos reais (acima da inflação) se fosse investido, utilizando as séries históricas de **CDI (4391)** e **IPCA (433)** do Sistema Gerenciador de Séries Temporais (SGS) do Banco Central do Brasil.

## Pré-requisitos

- **Java 21** instalado (`java -version`).
- Maven Wrapper já incluído no projeto (`./mvnw` ou `.\mvnw.cmd`).

## Como rodar a aplicação

No Linux/macOS:
```bash
./mvnw spring-boot:run
```

No Windows (PowerShell/CMD):
```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação sobe por padrão em `http://localhost:8080`.
- Healthcheck: `GET http://localhost:8080/actuator/health` → `{"status":"UP"}`

## Como rodar os testes

No Linux/macOS:
```bash
./mvnw test
```

No Windows (PowerShell/CMD):
```powershell
.\mvnw.cmd test
```

Para consultar manualmente o SGS do Banco Central e imprimir o CDI e IPCA anuais reais acumulados nos últimos 12 meses:
```powershell
.\mvnw.cmd -q exec:java "-Dexec.mainClass=br.com.montanhadegastos.taxas.BcbSgsClient"
```

## Endpoints e Exemplos

### 1. Listar hábitos predefinidos (`GET /api/habitos`)

**Resposta (`200 OK`):**
```json
[
  {
    "id": "CAFE",
    "nome": "Café",
    "valorPadrao": 8.00,
    "frequenciaPadrao": "DIA",
    "rotuloItem": "cafés"
  },
  {
    "id": "DELIVERY",
    "nome": "Delivery",
    "valorPadrao": 45.00,
    "frequenciaPadrao": "SEMANA",
    "rotuloItem": "pedidos"
  },
  {
    "id": "FUMO",
    "nome": "Fumo",
    "valorPadrao": 12.00,
    "frequenciaPadrao": "DIA",
    "rotuloItem": "maços de cigarro"
  },
  {
    "id": "COMPRAS",
    "nome": "Compras",
    "valorPadrao": 300.00,
    "frequenciaPadrao": "MES",
    "rotuloItem": "compras"
  }
]
```

### 2. Simular custo e investimento (`POST /api/simulacao`)

**Requisição:**
```json
{
  "habito": "CAFE",
  "valor": 8.00,
  "frequencia": "DIA",
  "anos": 30
}
```

**Resposta (`200 OK`):**
```json
{
  "custoBruto": 87600.00,
  "quantidadeItens": 10950,
  "rotuloItem": "cafés",
  "montanteInvestido": 166746.89,
  "multiplicador": 1.9,
  "macosCedulas": 16,
  "taxaRealAnual": 0.04000000,
  "origemTaxa": "bcb",
  "fraseImpacto": "Em 30 anos, seus 10.950 cafés custam R$ 87.600 e, investidos, virariam R$ 166.746,89."
}
```

> Se a API do Banco Central estiver indisponível e não houver cache válido (TTL de 24h), a simulação utiliza automaticamente a taxa real anual padrão configurada em `application.yml` (`4,0%` ao ano) e retorna `"origemTaxa": "padrao"`, nunca propagando erro 500 ao usuário.

## Explicação das Fórmulas de Cálculo

Todos os cálculos financeiros utilizam `BigDecimal` com arredondamento `HALF_UP` (2 casas decimais para valores monetários e 8 casas decimais para taxas e fatores mensais):

1. **Ocorrências por mês (`Frequencia`):**
   - `DIA` = $365 / 12 \approx 30{,}41666667$
   - `SEMANA` = $52 / 12 \approx 4{,}33333333$
   - `MES` = $1$

2. **Custo bruto e quantidade de itens (`CalculadoraCustoBruto`):**
   - $\text{meses} = \text{anos} \times 12$
   - $\text{valorMensal} = \text{valor} \times \text{ocorrenciasPorMes}$
   - $\text{custoBruto} = \text{valor} \times \text{ocorrenciasPorMes} \times \text{meses}$
   - $\text{quantidadeItens} = \text{round}(\text{ocorrenciasPorMes} \times \text{meses})$

3. **Taxa de juros real pela equação de Fisher (`CalculadoraTaxaReal`):**
   - Acumulam-se os últimos 12 meses do CDI mensal (série 4391) e do IPCA mensal (série 433):
     $$\text{taxaAnual} = \prod_{k=1}^{12} \left(1 + \frac{\text{percentualMensal}_k}{100}\right) - 1$$
   - Taxa real anual descontando a inflação:
     $$\text{taxaRealAnual} = \frac{1 + \text{CDI}_{\text{anual}}}{1 + \text{IPCA}_{\text{anual}}} - 1$$
   - Taxa real mensal equivalente:
     $$\text{taxaRealMensal} = (1 + \text{taxaRealAnual})^{1/12} - 1$$

4. **Montante investido, multiplicador e maços de cédulas (`CalculadoraMontante`):**
   - Série uniforme de aportes mensais a juros compostos:
     $$\text{montanteInvestido} = \text{valorMensal} \times \frac{(1 + \text{taxaRealMensal})^{\text{meses}} - 1}{\text{taxaRealMensal}}$$
     *(Quando $\text{taxaRealMensal} = 0$, utiliza-se $\text{valorMensal} \times \text{meses}$.)*
   - $\text{multiplicador} = \text{montanteInvestido} / \text{custoBruto}$ (1 casa decimal).
   - $\text{macosCedulas} = \lfloor \text{montanteInvestido} / 10.000 \rfloor$ (cada maço representa R$ 10.000 em notas de R$ 100).

