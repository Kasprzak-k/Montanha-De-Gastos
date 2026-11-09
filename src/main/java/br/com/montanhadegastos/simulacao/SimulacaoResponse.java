package br.com.montanhadegastos.simulacao;

import java.math.BigDecimal;

public record SimulacaoResponse(
        BigDecimal custoBruto,
        long quantidadeItens,
        String rotuloItem,
        BigDecimal montanteInvestido,
        BigDecimal multiplicador,
        long macosCedulas,
        BigDecimal taxaRealAnual,
        String origemTaxa,
        String fraseImpacto
) {
}

