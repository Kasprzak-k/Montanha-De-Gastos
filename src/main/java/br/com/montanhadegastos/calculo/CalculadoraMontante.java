package br.com.montanhadegastos.calculo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

public class CalculadoraMontante {

    private static final BigDecimal VALOR_MACO = new BigDecimal("10000");

    public ResultadoMontante calcular(ResultadoCustoBruto custoBruto, ResultadoTaxaReal taxaReal, int anos) {
        Objects.requireNonNull(custoBruto, "custoBruto não pode ser nulo");
        Objects.requireNonNull(taxaReal, "taxaReal não pode ser nula");
        if (anos <= 0) {
            throw new IllegalArgumentException("anos deve ser maior que zero");
        }

        int meses = anos * 12;
        BigDecimal valorMensal = custoBruto.valorMensal();
        BigDecimal taxaMensal = taxaReal.taxaRealMensal();

        BigDecimal montanteInvestido;
        if (taxaMensal.compareTo(BigDecimal.ZERO) == 0) {
            montanteInvestido = valorMensal.multiply(BigDecimal.valueOf(meses))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            BigDecimal potencia = BigDecimal.ONE.add(taxaMensal)
                    .pow(meses, MathContext.DECIMAL128);
            BigDecimal fatorAcumulacao = potencia.subtract(BigDecimal.ONE)
                    .divide(taxaMensal, 12, RoundingMode.HALF_UP);
            montanteInvestido = valorMensal.multiply(fatorAcumulacao)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal multiplicador = montanteInvestido
                .divide(custoBruto.custoBruto(), 1, RoundingMode.HALF_UP);

        long macosCedulas = montanteInvestido
                .divide(VALOR_MACO, 0, RoundingMode.FLOOR)
                .longValueExact();

        return new ResultadoMontante(montanteInvestido, multiplicador, macosCedulas);
    }
}

