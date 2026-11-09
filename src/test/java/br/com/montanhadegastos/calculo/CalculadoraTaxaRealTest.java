package br.com.montanhadegastos.calculo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculadoraTaxaRealTest {

    private final CalculadoraTaxaReal calculadora = new CalculadoraTaxaReal();

    @Test
    @DisplayName("Deve calcular taxa real anual e mensal para caso conhecido (CDI 10,4%, IPCA 6,15384615% -> ~4% real a.a.)")
    void deveCalcularCasoConhecido() {
        // (1 + 0,0816) / (1 + 0,04) - 1 = 1,0816 / 1,04 - 1 = 0,04 (4% a.a. exatos)
        // taxaRealMensal = (1,04)^(1/12) - 1 = 0,00327374
        ResultadoTaxaReal resultado = calculadora.calcular(
                new BigDecimal("0.0816"),
                new BigDecimal("0.0400")
        );

        assertEquals(new BigDecimal("0.04000000"), resultado.taxaRealAnual());
        assertEquals(new BigDecimal("0.00327374"), resultado.taxaRealMensal());
    }

    @Test
    @DisplayName("Deve retornar taxa real zero quando CDI e IPCA forem iguais")
    void deveCalcularTaxaRealZero() {
        ResultadoTaxaReal resultado = calculadora.calcular(
                new BigDecimal("0.0500"),
                new BigDecimal("0.0500")
        );

        assertEquals(0, resultado.taxaRealAnual().compareTo(BigDecimal.ZERO));
        assertEquals(0, resultado.taxaRealMensal().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Deve calcular taxa real negativa quando IPCA superar o CDI")
    void deveCalcularTaxaRealNegativa() {
        // (1 + 0,04) / (1 + 0,10) - 1 = 1,04 / 1,10 - 1 = -0,05454545
        // taxaRealMensal = (0,94545455)^(1/12) - 1 = -0,00466322
        ResultadoTaxaReal resultado = calculadora.calcular(
                new BigDecimal("0.0400"),
                new BigDecimal("0.1000")
        );

        assertEquals(new BigDecimal("-0.05454545"), resultado.taxaRealAnual());
        assertEquals(new BigDecimal("-0.00466322"), resultado.taxaRealMensal());
        assertTrue(resultado.taxaRealAnual().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(resultado.taxaRealMensal().compareTo(BigDecimal.ZERO) < 0);
    }
}
