package br.com.montanhadegastos.calculo;

import br.com.montanhadegastos.habitos.Frequencia;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class CalculadoraCustoBruto {

    public ResultadoCustoBruto calcular(BigDecimal valor, Frequencia frequencia, int anos) {
        Objects.requireNonNull(valor, "valor não pode ser nulo");
        Objects.requireNonNull(frequencia, "frequencia não pode ser nula");
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("valor deve ser maior que zero");
        }
        if (anos <= 0) {
            throw new IllegalArgumentException("anos deve ser maior que zero");
        }

        BigDecimal meses = BigDecimal.valueOf((long) anos * 12);
        BigDecimal ocorrenciasPorMes = frequencia.ocorrenciasPorMes();

        BigDecimal valorMensalSemArredondar = valor.multiply(ocorrenciasPorMes);
        BigDecimal valorMensal = valorMensalSemArredondar.setScale(2, RoundingMode.HALF_UP);
        BigDecimal custoBruto = valorMensalSemArredondar.multiply(meses).setScale(2, RoundingMode.HALF_UP);
        long quantidadeItens = ocorrenciasPorMes.multiply(meses)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        return new ResultadoCustoBruto(valorMensal, custoBruto, quantidadeItens);
    }
}

