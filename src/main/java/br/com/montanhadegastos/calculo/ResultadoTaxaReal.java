package br.com.montanhadegastos.calculo;

import java.math.BigDecimal;

public record ResultadoTaxaReal(
        BigDecimal taxaRealAnual,
        BigDecimal taxaRealMensal
) {
}

