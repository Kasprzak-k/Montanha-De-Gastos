package br.com.montanhadegastos.habitos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HabitoServiceTest {

    private final HabitoService service = new HabitoService(new HabitosProperties(null));

    @Test
    @DisplayName("Deve listar os 4 hábitos padrão")
    void deveListarQuatroHabitos() {
        List<Habito> habitos = service.listar();

        assertEquals(4, habitos.size());
        assertEquals("CAFE", habitos.get(0).id());
        assertEquals("DELIVERY", habitos.get(1).id());
        assertEquals("FUMO", habitos.get(2).id());
        assertEquals("COMPRAS", habitos.get(3).id());
    }

    @Test
    @DisplayName("Deve buscar hábito por id válido com seus valores padrão")
    void deveBuscarPorIdValido() {
        Habito cafe = service.buscarPorId("CAFE");
        assertEquals("Café", cafe.nome());
        assertEquals(new BigDecimal("8.00"), cafe.valorPadrao());
        assertEquals(Frequencia.DIA, cafe.frequenciaPadrao());
        assertEquals("cafés", cafe.rotuloItem());

        Habito delivery = service.buscarPorId("delivery");
        assertEquals("Delivery", delivery.nome());
        assertEquals(new BigDecimal("45.00"), delivery.valorPadrao());
        assertEquals(Frequencia.SEMANA, delivery.frequenciaPadrao());
        assertEquals("pedidos", delivery.rotuloItem());
    }

    @Test
    @DisplayName("Deve lançar erro claro ao buscar id inválido")
    void deveLancarErroParaIdInvalido() {
        HabitoNaoEncontradoException ex = assertThrows(
                HabitoNaoEncontradoException.class,
                () -> service.buscarPorId("ACADEMIA")
        );

        assertTrue(ex.getMessage().contains("ACADEMIA"));
    }
}

