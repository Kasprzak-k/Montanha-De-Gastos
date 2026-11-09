package br.com.montanhadegastos.calculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class CalculadoraTaxaReal {

    public static final int ESCALA_TAXA = 8;

    public ResultadoTaxaReal calcular(BigDecimal cdiAnual, BigDecimal ipcaAnual) {
        Objects.requireNonNull(cdiAnual, "cdiAnual não pode ser nulo");
        Objects.requireNonNull(ipcaAnual, "ipcaAnual não pode ser nulo");

        BigDecimal divisor = BigDecimal.ONE.add(ipcaAnual);
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("1 + ipcaAnual deve ser maior que zero");
        }

        BigDecimal taxaRealAnual = BigDecimal.ONE.add(cdiAnual)
                .divide(divisor, ESCALA_TAXA, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .setScale(ESCALA_TAXA, RoundingMode.HALF_UP);

        return calcularAPartirDaAnual(taxaRealAnual);
    }

    public ResultadoTaxaReal calcularAPartirDaAnual(BigDecimal taxaRealAnual) {
        Objects.requireNonNull(taxaRealAnual, "taxaRealAnual não pode ser nula");
        BigDecimal anualNormalizada = taxaRealAnual.setScale(ESCALA_TAXA, RoundingMode.HALF_UP);

        // BigDecimal não possui potência fracionária ((1 + i)^(1/12)); usamos Math.pow com double apenas neste passo
        // e convertemos imediatamente de volta para BigDecimal com escala fixa de 8 casas (HALF_UP).
        double fatorAnual = BigDecimal.ONE.add(anualNormalizada).doubleValue();
        double taxaMensalDouble = Math.pow(fatorAnual, 1.0 / 12.0) - 1.0;
        BigDecimal taxaRealMensal = BigDecimal.valueOf(taxaMensalDouble)
                .setScale(ESCALA_TAXA, RoundingMode.HALF_UP);

        return new ResultadoTaxaReal(anualNormalizada, taxaRealMensal);
    }
}

