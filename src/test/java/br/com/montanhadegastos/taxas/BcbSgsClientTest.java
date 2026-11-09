package br.com.montanhadegastos.taxas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BcbSgsClientTest {

    private MockRestServiceServer mockServer;
    private BcbSgsClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.bcb.gov.br");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new BcbSgsClient(builder.build());
    }

    @Test
    @DisplayName("Deve fazer parsing do JSON e acumular 12 meses de CDI e IPCA")
    void deveAcumular12MesesDeCdiEIpca() {
        // 12 meses de 1.00% a.m. -> (1.01)^12 - 1 = 0.12682503
        String jsonCdi = """
                [
                  {"data":"01/01/2025","valor":"1.00"},
                  {"data":"01/02/2025","valor":"1.00"},
                  {"data":"01/03/2025","valor":"1.00"},
                  {"data":"01/04/2025","valor":"1.00"},
                  {"data":"01/05/2025","valor":"1.00"},
                  {"data":"01/06/2025","valor":"1.00"},
                  {"data":"01/07/2025","valor":"1.00"},
                  {"data":"01/08/2025","valor":"1.00"},
                  {"data":"01/09/2025","valor":"1.00"},
                  {"data":"01/10/2025","valor":"1.00"},
                  {"data":"01/11/2025","valor":"1.00"},
                  {"data":"01/12/2025","valor":"1.00"}
                ]
                """;
        // 12 meses de 0.50% a.m. -> (1.005)^12 - 1 = 0.06167781
        String jsonIpca = """
                [
                  {"data":"01/01/2025","valor":"0.50"},
                  {"data":"01/02/2025","valor":"0.50"},
                  {"data":"01/03/2025","valor":"0.50"},
                  {"data":"01/04/2025","valor":"0.50"},
                  {"data":"01/05/2025","valor":"0.50"},
                  {"data":"01/06/2025","valor":"0.50"},
                  {"data":"01/07/2025","valor":"0.50"},
                  {"data":"01/08/2025","valor":"0.50"},
                  {"data":"01/09/2025","valor":"0.50"},
                  {"data":"01/10/2025","valor":"0.50"},
                  {"data":"01/11/2025","valor":"0.50"},
                  {"data":"01/12/2025","valor":"0.50"}
                ]
                """;

        mockServer.expect(requestTo("https://api.bcb.gov.br/dados/serie/bcdata.sgs.4391/dados/ultimos/12?formato=json"))
                .andRespond(withSuccess(jsonCdi, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo("https://api.bcb.gov.br/dados/serie/bcdata.sgs.433/dados/ultimos/12?formato=json"))
                .andRespond(withSuccess(jsonIpca, MediaType.APPLICATION_JSON));

        TaxasAnuaisBcb taxas = client.buscarTaxasAnuais();

        assertEquals(new BigDecimal("0.12682503"), taxas.cdiAnual());
        assertEquals(new BigDecimal("0.06167781"), taxas.ipcaAnual());
        mockServer.verify();
    }

    @Test
    @DisplayName("Deve encapsular erro de rede ou HTTP em FalhaConsultaBcbException")
    void deveTratarErroDeRede() {
        mockServer.expect(requestTo("https://api.bcb.gov.br/dados/serie/bcdata.sgs.4391/dados/ultimos/12?formato=json"))
                .andRespond(withServerError());

        assertThrows(FalhaConsultaBcbException.class, () -> client.buscarTaxasAnuais());
        mockServer.verify();
    }
}

