package br.com.montanhadegastos.calculo;

import java.math.BigDecimal;

public record ResultadoMontante(
        BigDecimal montanteInvestido,
        BigDecimal multiplicador,
        long macosCedulas
) {
}

