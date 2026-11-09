package br.com.montanhadegastos.api;

import br.com.montanhadegastos.taxas.BcbSgsClient;
import br.com.montanhadegastos.taxas.FalhaConsultaBcbException;
import br.com.montanhadegastos.taxas.TaxaRealService;
import br.com.montanhadegastos.taxas.TaxasAnuaisBcb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SimulacaoIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaxaRealService taxaRealService;

    @MockitoBean
    private BcbSgsClient bcbSgsClient;

    @BeforeEach
    void setUp() {
        taxaRealService.limparCache();
    }

    @Test
    @DisplayName("Ponta a ponta: POST /api/simulacao calcula tudo do início ao fim com taxas do BCB simuladas")
    void deveExecutarPostCompletoComTaxasBcb() throws Exception {
        // CDI = 8,16% a.a., IPCA = 4,00% a.a. -> Taxa real anual = (1,0816 / 1,04) - 1 = 4,00% a.a.
        when(bcbSgsClient.buscarTaxasAnuais()).thenReturn(
                new TaxasAnuaisBcb(new BigDecimal("0.0816"), new BigDecimal("0.0400"))
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
                .andExpect(jsonPath("$.taxaRealAnual").value(0.04))
                .andExpect(jsonPath("$.origemTaxa").value("bcb"))
                .andExpect(jsonPath("$.fraseImpacto").value(
                        "Em 30 anos, seus 10.950 cafés custam R$ 87.600 e, investidos, virariam R$ 166.746,89."
                ));
    }

    @Test
    @DisplayName("Ponta a ponta: quando o Banco Central está fora do ar, POST /api/simulacao responde 200 com origemTaxa 'padrao'")
    void deveResponder200ComOrigemPadraoQuandoBcbForaDoAr() throws Exception {
        when(bcbSgsClient.buscarTaxasAnuais())
                .thenThrow(new FalhaConsultaBcbException("Serviço SGS indisponível"));

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
                .andExpect(jsonPath("$.montanteInvestido").value(166746.89))
                .andExpect(jsonPath("$.taxaRealAnual").value(0.04))
                .andExpect(jsonPath("$.origemTaxa").value("padrao"));
    }
}

