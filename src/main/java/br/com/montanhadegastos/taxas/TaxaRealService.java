package br.com.montanhadegastos.taxas;

import br.com.montanhadegastos.calculo.CalculadoraTaxaReal;
import br.com.montanhadegastos.calculo.ResultadoTaxaReal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
@EnableConfigurationProperties(TaxasProperties.class)
public class TaxaRealService {

    private static final Logger log = LoggerFactory.getLogger(TaxaRealService.class);

    private final BcbSgsClient bcbSgsClient;
    private final TaxasProperties properties;
    private final Clock clock;
    private final CalculadoraTaxaReal calculadoraTaxaReal = new CalculadoraTaxaReal();

    private record CacheEntry(TaxaRealVigente taxa, Instant expiraEm) {}

    // shortcut: cache em memória local da JVM, migrar para cache compartilhado se rodar múltiplas instâncias
    private volatile CacheEntry cache;

    @Autowired
    public TaxaRealService(BcbSgsClient bcbSgsClient, TaxasProperties properties) {
        this(bcbSgsClient, properties, Clock.systemUTC());
    }

    public TaxaRealService(BcbSgsClient bcbSgsClient, TaxasProperties properties, Clock clock) {
        this.bcbSgsClient = bcbSgsClient;
        this.properties = properties;
        this.clock = clock;
    }

    public synchronized void limparCache() {
        this.cache = null;
    }

    public synchronized TaxaRealVigente obterTaxaReal() {
        Instant agora = clock.instant();
        if (cache != null && agora.isBefore(cache.expiraEm())) {
            return cache.taxa();
        }

        try {
            TaxasAnuaisBcb taxasBcb = bcbSgsClient.buscarTaxasAnuais();
            ResultadoTaxaReal calculada = calculadoraTaxaReal.calcular(taxasBcb.cdiAnual(), taxasBcb.ipcaAnual());
            TaxaRealVigente vigente = new TaxaRealVigente(
                    calculada.taxaRealAnual(),
                    calculada.taxaRealMensal(),
                    TaxaRealVigente.ORIGEM_BCB
            );
            cache = new CacheEntry(vigente, agora.plus(properties.cacheTtl()));
            return vigente;
        } catch (Exception e) {
            log.warn("Falha ao obter taxas do Banco Central; utilizando taxa real padrão ({})",
                    properties.taxaRealAnualPadrao(), e);
            ResultadoTaxaReal padrao = calculadoraTaxaReal.calcularAPartirDaAnual(properties.taxaRealAnualPadrao());
            return new TaxaRealVigente(
                    padrao.taxaRealAnual(),
                    padrao.taxaRealMensal(),
                    TaxaRealVigente.ORIGEM_PADRAO
            );
        }
    }
}
