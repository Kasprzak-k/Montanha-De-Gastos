package br.com.montanhadegastos.api;

import br.com.montanhadegastos.habitos.Frequencia;
import br.com.montanhadegastos.habitos.Habito;
import br.com.montanhadegastos.habitos.HabitoNaoEncontradoException;
import br.com.montanhadegastos.habitos.HabitoService;
import br.com.montanhadegastos.simulacao.SimulacaoRequest;
import br.com.montanhadegastos.simulacao.SimulacaoResponse;
import br.com.montanhadegastos.simulacao.SimulacaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimulacaoController.class)
@Import(GlobalExceptionHandler.class)
class SimulacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HabitoService habitoService;

    @MockitoBean
    private SimulacaoService simulacaoService;

    @Test
    @DisplayName("GET /api/habitos deve devolver a lista de presets")
    void deveListarHabitosComSucesso() throws Exception {
        when(habitoService.listar()).thenReturn(List.of(
                new Habito("CAFE", "Café", new BigDecimal("8.00"), Frequencia.DIA, "cafés"),
                new Habito("DELIVERY", "Delivery", new BigDecimal("45.00"), Frequencia.SEMANA, "pedidos")
        ));

        mockMvc.perform(get("/api/habitos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("CAFE"))
                .andExpect(jsonPath("$[0].rotuloItem").value("cafés"));
    }

    @Test
    @DisplayName("POST /api/simulacao com dados válidos deve devolver 200 e o resultado da simulação")
    void deveSimularComSucesso() throws Exception {
        when(simulacaoService.simular(any(SimulacaoRequest.class))).thenReturn(
                new SimulacaoResponse(
                        new BigDecimal("87600.00"),
                        10950L,
                        "cafés",
                        new BigDecimal("166746.89"),
                        new BigDecimal("1.9"),
                        16L,
                        new BigDecimal("0.04000000"),
                        "bcb",
                        "Em 30 anos, seus 10.950 cafés custam R$ 87.600 e, investidos, virariam R$ 166.746,89."
                )
        );

        String payload = """
                {
                  "habito": "CAFE",
                  "valor": 8.00,
                  "frequencia": "DIA",
                  "anos": 30
                }
                """;

        mockMvc.perform(post("/api/simulacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custoBruto").value(87600.00))
                .andExpect(jsonPath("$.quantidadeItens").value(10950))
                .andExpect(jsonPath("$.rotuloItem").value("cafés"))
                .andExpect(jsonPath("$.montanteInvestido").value(166746.89))
                .andExpect(jsonPath("$.multiplicador").value(1.9))
                .andExpect(jsonPath("$.macosCedulas").value(16))
                .andExpect(jsonPath("$.origemTaxa").value("bcb"));
    }

    @Test
    @DisplayName("POST /api/simulacao com valor negativo deve devolver 400 e mensagem clara")
    void deveRejeitarValorNegativo() throws Exception {
        String payload = """
                {
                  "habito": "CAFE",
                  "valor": -5.00,
                  "frequencia": "DIA",
                  "anos": 30
                }
                """;

        mockMvc.perform(post("/api/simulacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("VALIDACAO_INVALIDA"))
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    @DisplayName("POST /api/simulacao com anos fora do limite (1..50) deve devolver 400")
    void deveRejeitarAnosForaDoLimite() throws Exception {
        String payload = """
                {
                  "habito": "CAFE",
                  "valor": 8.00,
                  "frequencia": "DIA",
                  "anos": 60
                }
                """;

        mockMvc.perform(post("/api/simulacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("VALIDACAO_INVALIDA"))
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    @DisplayName("POST /api/simulacao com hábito inexistente deve devolver 400 com erro padronizado")
    void deveRejeitarHabitoInexistente() throws Exception {
        when(simulacaoService.simular(any(SimulacaoRequest.class)))
                .thenThrow(new HabitoNaoEncontradoException("ACADEMIA"));

        String payload = """
                {
                  "habito": "ACADEMIA",
                  "valor": 100.00,
                  "frequencia": "MES",
                  "anos": 10
                }
                """;

        mockMvc.perform(post("/api/simulacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("HABITO_INEXISTENTE"))
                .andExpect(jsonPath("$.mensagem").value("Hábito inválido ou não encontrado: ACADEMIA"));
    }

    @Test
    @DisplayName("POST /api/simulacao com JSON malformado deve devolver 400")
    void deveRejeitarJsonMalformado() throws Exception {
        String payload = "{ \"habito\": \"CAFE\", \"valor\": ";

        mockMvc.perform(post("/api/simulacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("REQUISICAO_MALFORMADA"))
                .andExpect(jsonPath("$.mensagem").exists());
    }
}

