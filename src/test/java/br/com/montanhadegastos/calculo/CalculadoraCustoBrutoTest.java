package br.com.montanhadegastos.calculo;

import br.com.montanhadegastos.habitos.Frequencia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraCustoBrutoTest {

    private final CalculadoraCustoBruto calculadora = new CalculadoraCustoBruto();

    @Test
    @DisplayName("Café R$ 8 por dia em 30 anos deve dar 10.950 itens e custo bruto de R$ 87.600,00")
    void deveCalcularCasoDiarioCafe30Anos() {
        // Conta à mão:
        // ocorrenciasPorMes = 365 / 12 = 30,41666667
        // valorMensal = 8,00 * 30,41666667 = 243,33
        // meses = 30 * 12 = 360
        // quantidadeItens = 30,41666667 * 360 = 10.950
        // custoBruto = 8,00 * 10.950 = 87.600,00
        ResultadoCustoBruto resultado = calculadora.calcular(new BigDecimal("8.00"), Frequencia.DIA, 30);

        assertEquals(new BigDecimal("243.33"), resultado.valorMensal());
        assertEquals(new BigDecimal("87600.00"), resultado.custoBruto());
        assertEquals(10950L, resultado.quantidadeItens());
    }

    @Test
    @DisplayName("Delivery R$ 45 por semana em 10 anos deve dar 520 itens e custo bruto de R$ 23.400,00")
    void deveCalcularCasoSemanalConferidoAMao() {
        // Conta à mão:
        // ocorrenciasPorMes = 52 / 12 = 4,33333333
        // valorMensal = 45,00 * 4,33333333 = 195,00
        // meses = 10 * 12 = 120
        // quantidadeItens = 4,33333333 * 120 = 520
        // custoBruto = 195,00 * 120 = 23.400,00
        ResultadoCustoBruto resultado = calculadora.calcular(new BigDecimal("45.00"), Frequencia.SEMANA, 10);

        assertEquals(new BigDecimal("195.00"), resultado.valorMensal());
        assertEquals(new BigDecimal("23400.00"), resultado.custoBruto());
        assertEquals(520L, resultado.quantidadeItens());
    }

    @Test
    @DisplayName("Compras R$ 300 por mês em 5 anos deve dar 60 itens e custo bruto de R$ 18.000,00")
    void deveCalcularCasoMensalConferidoAMao() {
        // Conta à mão:
        // ocorrenciasPorMes = 1
        // valorMensal = 300,00 * 1 = 300,00
        // meses = 5 * 12 = 60
        // quantidadeItens = 1 * 60 = 60
        // custoBruto = 300,00 * 60 = 18.000,00
        ResultadoCustoBruto resultado = calculadora.calcular(new BigDecimal("300.00"), Frequencia.MES, 5);

        assertEquals(new BigDecimal("300.00"), resultado.valorMensal());
        assertEquals(new BigDecimal("18000.00"), resultado.custoBruto());
        assertEquals(60L, resultado.quantidadeItens());
    }
}

