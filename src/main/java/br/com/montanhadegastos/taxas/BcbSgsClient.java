package br.com.montanhadegastos.taxas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class BcbSgsClient {

    public static final int SERIE_IPCA_MENSAL = 433;
    public static final int SERIE_CDI_MENSAL = 4391;
    private static final String BASE_URL = "https://api.bcb.gov.br";
    private static final ParameterizedTypeReference<List<PontoSerieSgs>> LISTA_PONTOS =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    @Autowired
    public BcbSgsClient(RestClient.Builder builder) {
        this(builder.baseUrl(BASE_URL).build());
    }

    public BcbSgsClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public TaxasAnuaisBcb buscarTaxasAnuais() {
        BigDecimal cdiAnual = buscarTaxaAcumulada12Meses(SERIE_CDI_MENSAL);
        BigDecimal ipcaAnual = buscarTaxaAcumulada12Meses(SERIE_IPCA_MENSAL);
        return new TaxasAnuaisBcb(cdiAnual, ipcaAnual);
    }

    public static void main(String[] args) {
        BcbSgsClient client = new BcbSgsClient(RestClient.builder());
        TaxasAnuaisBcb taxas = client.buscarTaxasAnuais();
        System.out.println("CDI anual acumulado (12m): " + taxas.cdiAnual());
        System.out.println("IPCA anual acumulado (12m): " + taxas.ipcaAnual());
    }

    BigDecimal buscarTaxaAcumulada12Meses(int codigoSerie) {
        try {
            List<PontoSerieSgs> pontos = restClient.get()
                    .uri("/dados/serie/bcdata.sgs.{serie}/dados/ultimos/12?formato=json", codigoSerie)
                    .retrieve()
                    .body(LISTA_PONTOS);

            if (pontos == null || pontos.isEmpty()) {
                throw new FalhaConsultaBcbException("Resposta vazia do SGS para a série " + codigoSerie);
            }

            BigDecimal fatorAcumulado = BigDecimal.ONE;
            for (PontoSerieSgs ponto : pontos) {
                if (ponto == null || ponto.valor() == null || ponto.valor().isBlank()) {
                    throw new FalhaConsultaBcbException("Valor inválido na série " + codigoSerie);
                }
                BigDecimal percentualMensal = new BigDecimal(ponto.valor().trim().replace(",", "."));
                BigDecimal taxaMensal = percentualMensal.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
                fatorAcumulado = fatorAcumulado.multiply(BigDecimal.ONE.add(taxaMensal));
            }

            return fatorAcumulado.subtract(BigDecimal.ONE).setScale(8, RoundingMode.HALF_UP);
        } catch (FalhaConsultaBcbException e) {
            throw e;
        } catch (Exception e) {
            throw new FalhaConsultaBcbException("Erro ao consultar série " + codigoSerie + " no SGS do Banco Central", e);
        }
    }
}
