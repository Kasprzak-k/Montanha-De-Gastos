package br.com.montanhadegastos.calculo;

import java.math.BigDecimal;

public record ResultadoCustoBruto(
        BigDecimal valorMensal,
        BigDecimal custoBruto,
        long quantidadeItens
) {
}

