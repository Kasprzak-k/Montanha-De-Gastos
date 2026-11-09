package br.com.montanhadegastos.taxas;

import java.math.BigDecimal;

public record TaxaRealVigente(
        BigDecimal taxaRealAnual,
        BigDecimal taxaRealMensal,
        String origem
) {
    public static final String ORIGEM_BCB = "bcb";
    public static final String ORIGEM_PADRAO = "padrao";
}

