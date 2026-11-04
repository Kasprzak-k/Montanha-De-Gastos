package br.com.montanhadegastos.habitos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FrequenciaTest {

    @Test
    @DisplayName("DIA deve devolver 365/12 com escala de 8 casas decimais")
    void diaDeveDevolver365DivididoPor12() {
        BigDecimal esperado = new BigDecimal("30.41666667");
        BigDecimal obtido = Frequencia.DIA.getOcorrenciasPorMes();

        assertEquals(esperado, obtido);
        assertEquals(8, obtido.scale());
        assertEquals(esperado, Frequencia.DIA.ocorrenciasPorMes());
    }

    @Test
    @DisplayName("SEMANA deve devolver 52/12 com escala de 8 casas decimais")
    void semanaDeveDevolver52DivididoPor12() {
        BigDecimal esperado = new BigDecimal("4.33333333");
        BigDecimal obtido = Frequencia.SEMANA.getOcorrenciasPorMes();

        assertEquals(esperado, obtido);
        assertEquals(8, obtido.scale());
        assertEquals(esperado, Frequencia.SEMANA.ocorrenciasPorMes());
    }

    @Test
    @DisplayName("MES deve devolver 1 com escala de 8 casas decimais")
    void mesDeveDevolverUm() {
        BigDecimal esperado = new BigDecimal("1.00000000");
        BigDecimal obtido = Frequencia.MES.getOcorrenciasPorMes();

        assertEquals(esperado, obtido);
        assertEquals(8, obtido.scale());
        assertEquals(0, obtido.compareTo(BigDecimal.ONE));
        assertEquals(esperado, Frequencia.MES.ocorrenciasPorMes());
    }

    @Test
    @DisplayName("Deve conter todas as frequências esperadas")
    void deveConterTodasFrequencias() {
        assertEquals(3, Frequencia.values().length);
        assertNotNull(Frequencia.valueOf("DIA"));
        assertNotNull(Frequencia.valueOf("SEMANA"));
        assertNotNull(Frequencia.valueOf("MES"));
    }
}

