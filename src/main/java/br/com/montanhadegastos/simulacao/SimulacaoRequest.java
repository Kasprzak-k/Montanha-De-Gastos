package br.com.montanhadegastos.simulacao;

import br.com.montanhadegastos.habitos.Frequencia;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SimulacaoRequest(
        @NotBlank(message = "O hábito é obrigatório")
        String habito,

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser de no mínimo R$ 0,01")
        @DecimalMax(value = "1000000.00", message = "O valor deve ser de no máximo R$ 1.000.000,00")
        BigDecimal valor,

        @NotNull(message = "A frequência é obrigatória")
        Frequencia frequencia,

        @NotNull(message = "O horizonte em anos é obrigatório")
        @Min(value = 1, message = "O horizonte deve ser de no mínimo 1 ano")
        @Max(value = 50, message = "O horizonte deve ser de no máximo 50 anos")
        Integer anos
) {
}

