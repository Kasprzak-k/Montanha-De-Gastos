package br.com.montanhadegastos.simulacao;

import br.com.montanhadegastos.habitos.Frequencia;
import br.com.montanhadegastos.habitos.HabitoService;
import br.com.montanhadegastos.habitos.HabitosProperties;
import br.com.montanhadegastos.taxas.TaxaRealService;
import br.com.montanhadegastos.taxas.TaxaRealVigente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimulacaoServiceTest {

    private TaxaRealService taxaRealService;
    private SimulacaoService simulacaoService;

    @BeforeEach
    void setUp() {
        HabitoService habitoService = new HabitoService(new HabitosProperties(null));
        taxaRealService = mock(TaxaRealService.class);
        FraseImpactoService fraseImpactoService = new FraseImpactoService();
        simulacaoService = new SimulacaoService(habitoService, taxaRealService, fraseImpactoService);

        when(taxaRealService.obterTaxaReal()).thenReturn(new TaxaRealVigente(
                new BigDecimal("0.04000000"),
                new BigDecimal("0.00327374"),
                "bcb"
        ));
    }

    @Test
    @DisplayName("Deve executar fluxo completo para o hábito CAFE com taxas simuladas")
    void deveSimularFluxoCompletoParaCafe() {
        SimulacaoRequest request = new SimulacaoRequest(
                "CAFE",
                new BigDecimal("8.00"),
                Frequencia.DIA,
                30
        );

        SimulacaoResponse response = simulacaoService.simular(request);

        assertEquals(new BigDecimal("87600.00"), response.custoBruto());
        assertEquals(10950L, response.quantidadeItens());
        assertEquals("cafés", response.rotuloItem());
        assertEquals(new BigDecimal("166746.89"), response.montanteInvestido());
        assertEquals(new BigDecimal("1.9"), response.multiplicador());
        assertEquals(16L, response.macosCedulas());
        assertEquals(new BigDecimal("0.04000000"), response.taxaRealAnual());
        assertEquals("bcb", response.origemTaxa());
        assertEquals(
                "Em 30 anos, seus 10.950 cafés custam R$ 87.600 e, investidos, virariam R$ 166.746,89.",
                response.fraseImpacto()
        );
    }

    @Test
    @DisplayName("Deve executar fluxo completo para outro hábito (DELIVERY)")
    void deveSimularFluxoCompletoParaDelivery() {
        SimulacaoRequest request = new SimulacaoRequest(
                "DELIVERY",
                new BigDecimal("45.00"),
                Frequencia.SEMANA,
                10
        );

        SimulacaoResponse response = simulacaoService.simular(request);

        assertEquals(new BigDecimal("23400.00"), response.custoBruto());
        assertEquals(520L, response.quantidadeItens());
        assertEquals("pedidos", response.rotuloItem());
        assertTrue(response.montanteInvestido().compareTo(response.custoBruto()) > 0);
        assertEquals(new BigDecimal("1.2"), response.multiplicador());
        assertEquals(2L, response.macosCedulas());
        assertEquals("bcb", response.origemTaxa());
        assertTrue(response.fraseImpacto().contains("520 pedidos no delivery"));
    }
}
