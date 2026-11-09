package br.com.montanhadegastos.calculo;

import br.com.montanhadegastos.habitos.Frequencia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculadoraMontanteTest {

    private final CalculadoraCustoBruto calculadoraCustoBruto = new CalculadoraCustoBruto();
    private final CalculadoraTaxaReal calculadoraTaxaReal = new CalculadoraTaxaReal();
    private final CalculadoraMontante calculadoraMontante = new CalculadoraMontante();

    @Test
    @DisplayName("Caso à mão: R$ 10 por dia, 30 anos, taxa real fixa de 4% ao ano")
    void deveCalcularCasoAMao() {
        // Caso conferido à mão:
        // 1. Entrada: R$ 10,00 por DIA durante 30 anos (360 meses), taxa real anual fixa = 4% (0,04)
        // 2. Custo bruto (Task 3):
        //    - ocorrenciasPorMes = 365 / 12 = 30,41666667
        //    - valorMensal = 10,00 * 30,41666667 = R$ 304,17
        //    - custoBruto = 10,00 * 30,41666667 * 360 = R$ 109.500,00
        //    - quantidadeItens = 10.950
        // 3. Taxa real mensal (Task 4):
        //    - taxaRealMensal = (1 + 0,04)^(1/12) - 1 = 0,00327374
        // 4. Montante investido (Task 5):
        //    - fator = ((1 + 0,00327374)^360 - 1) / 0,00327374 = 685,270609001545
        //    - montanteInvestido = 304,17 * 685,270609001545 = R$ 208.438,76
        //    - multiplicador = 208.438,76 / 109.500,00 = 1,90355... -> 1,9
        //    - macosCedulas = floor(208.438,76 / 10.000) = 20
        ResultadoCustoBruto custo = calculadoraCustoBruto.calcular(new BigDecimal("10.00"), Frequencia.DIA, 30);
        ResultadoTaxaReal taxa = calculadoraTaxaReal.calcularAPartirDaAnual(new BigDecimal("0.04"));

        ResultadoMontante resultado = calculadoraMontante.calcular(custo, taxa, 30);

        assertEquals(new BigDecimal("208438.76"), resultado.montanteInvestido());
        assertEquals(new BigDecimal("1.9"), resultado.multiplicador());
        assertEquals(20L, resultado.macosCedulas());
    }

    @Test
    @DisplayName("Deve usar valorMensal x meses quando a taxa mensal for zero")
    void deveCalcularComTaxaZero() {
        ResultadoCustoBruto custo = calculadoraCustoBruto.calcular(new BigDecimal("100.00"), Frequencia.MES, 10);
        ResultadoTaxaReal taxaZero = calculadoraTaxaReal.calcularAPartirDaAnual(BigDecimal.ZERO);

        ResultadoMontante resultado = calculadoraMontante.calcular(custo, taxaZero, 10);

        assertEquals(new BigDecimal("12000.00"), resultado.montanteInvestido());
        assertEquals(new BigDecimal("1.0"), resultado.multiplicador());
        assertEquals(1L, resultado.macosCedulas());
    }

    @Test
    @DisplayName("Deve calcular montante menor que o custo bruto quando a taxa real for negativa")
    void deveCalcularComTaxaNegativa() {
        ResultadoCustoBruto custo = calculadoraCustoBruto.calcular(new BigDecimal("200.00"), Frequencia.MES, 10);
        ResultadoTaxaReal taxaNegativa = calculadoraTaxaReal.calcular(
                new BigDecimal("0.0400"),
                new BigDecimal("0.1000")
        );

        ResultadoMontante resultado = calculadoraMontante.calcular(custo, taxaNegativa, 10);

        assertTrue(resultado.montanteInvestido().compareTo(custo.custoBruto()) < 0);
        assertEquals(new BigDecimal("18412.26"), resultado.montanteInvestido());
        assertEquals(new BigDecimal("0.8"), resultado.multiplicador());
        assertEquals(1L, resultado.macosCedulas());
    }

    @Test
    @DisplayName("Deve retornar 0 maços quando o montante investido for menor que R$ 10.000")
    void deveRetornarZeroMacosParaMontantePequeno() {
        ResultadoCustoBruto custo = calculadoraCustoBruto.calcular(new BigDecimal("50.00"), Frequencia.MES, 1);
        ResultadoTaxaReal taxa = calculadoraTaxaReal.calcularAPartirDaAnual(new BigDecimal("0.04"));

        ResultadoMontante resultado = calculadoraMontante.calcular(custo, taxa, 1);

        assertTrue(resultado.montanteInvestido().compareTo(new BigDecimal("10000.00")) < 0);
        assertEquals(0L, resultado.macosCedulas());
    }
}
