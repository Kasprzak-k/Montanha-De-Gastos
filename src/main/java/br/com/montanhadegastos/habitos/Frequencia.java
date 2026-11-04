package br.com.montanhadegastos.habitos;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Representa a frequência de ocorrência de um hábito e calcula as ocorrências equivalentes por mês.
 * <p>
 * Regras:
 * - DIA: 365 / 12
 * - SEMANA: 52 / 12
 * - MES: 1
 * <p>
 * Precisão definida: escala de 8 casas decimais com arredondamento HALF_UP.
 */
public enum Frequencia {
    DIA(365, 12),
    SEMANA(52, 12),
    MES(1, 1);

    public static final int ESCALA = 8;
    public static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_UP;

    private final BigDecimal ocorrenciasPorMes;

    Frequencia(int numerador, int denominador) {
        this.ocorrenciasPorMes = BigDecimal.valueOf(numerador)
                .divide(BigDecimal.valueOf(denominador), ESCALA, RoundingMode.HALF_UP);
    }

    public BigDecimal getOcorrenciasPorMes() {
        return ocorrenciasPorMes;
    }

    public BigDecimal ocorrenciasPorMes() {
        return ocorrenciasPorMes;
    }
}
