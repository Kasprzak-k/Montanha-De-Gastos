package br.com.montanhadegastos.habitos;

import java.math.BigDecimal;

public record Habito(
        String id,
        String nome,
        BigDecimal valorPadrao,
        Frequencia frequenciaPadrao,
        String rotuloItem
) {
}

