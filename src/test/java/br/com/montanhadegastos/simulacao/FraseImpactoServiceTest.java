package br.com.montanhadegastos.simulacao;

import br.com.montanhadegastos.habitos.Frequencia;
import br.com.montanhadegastos.habitos.Habito;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FraseImpactoServiceTest {

    private final FraseImpactoService service = new FraseImpactoService();

    @Test
    @DisplayName("Deve gerar frase para CAFE conforme exemplo da especificação")
    void deveGerarFraseParaCafe() {
        Habito cafe = new Habito("CAFE", "Café", new BigDecimal("8.00"), Frequencia.DIA, "cafés");

        String frase = service.gerarFrase(
                cafe,
                30,
                new BigDecimal("87600.00"),
                10950L,
                new BigDecimal("280000.00")
        );

        assertEquals("Em 30 anos, seus 10.950 cafés custam R$ 87.600 e, investidos, virariam R$ 280.000.", frase);
    }

    @Test
    @DisplayName("Deve gerar frases para DELIVERY, FUMO e COMPRAS")
    void deveGerarFrasesPorHabito() {
        Habito delivery = new Habito("DELIVERY", "Delivery", new BigDecimal("45.00"), Frequencia.SEMANA, "pedidos");
        Habito fumo = new Habito("FUMO", "Fumo", new BigDecimal("12.00"), Frequencia.DIA, "maços de cigarro");
        Habito compras = new Habito("COMPRAS", "Compras", new BigDecimal("300.00"), Frequencia.MES, "compras");

        String fraseDelivery = service.gerarFrase(delivery, 10, new BigDecimal("23400.00"), 520L, new BigDecimal("28725.50"));
        String fraseFumo = service.gerarFrase(fumo, 20, new BigDecimal("87600.00"), 7300L, new BigDecimal("134120.75"));
        String fraseCompras = service.gerarFrase(compras, 5, new BigDecimal("18000.00"), 60L, new BigDecimal("19890.10"));

        assertTrue(fraseDelivery.contains("520 pedidos no delivery"));
        assertTrue(fraseDelivery.contains("R$ 28.725,50"));
        assertTrue(fraseFumo.contains("7.300 maços de cigarro"));
        assertTrue(fraseFumo.contains("R$ 134.120,75"));
        assertEquals(
                "Em 5 anos, suas 60 compras custam R$ 18.000 e, investidas, virariam R$ 19.890,10.",
                fraseCompras
        );
    }

    @Test
    @DisplayName("Deve formatar valores pequenos e valores grandes no padrão brasileiro")
    void deveFormatarValoresPequenosEGrandes() {
        Habito cafe = new Habito("CAFE", "Café", new BigDecimal("8.00"), Frequencia.DIA, "cafés");

        String frasePequena = service.gerarFrase(
                cafe,
                1,
                new BigDecimal("12.50"),
                5L,
                new BigDecimal("12.85")
        );
        assertEquals("Em 1 ano, seus 5 cafés custam R$ 12,50 e, investidos, virariam R$ 12,85.", frasePequena);

        String fraseGrande = service.gerarFrase(
                cafe,
                50,
                new BigDecimal("1500000.00"),
                18250L,
                new BigDecimal("4567890.12")
        );
        assertEquals(
                "Em 50 anos, seus 18.250 cafés custam R$ 1.500.000 e, investidos, virariam R$ 4.567.890,12.",
                fraseGrande
        );
    }
}

