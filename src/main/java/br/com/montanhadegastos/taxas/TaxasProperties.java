package br.com.montanhadegastos.taxas;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.Duration;

@ConfigurationProperties(prefix = "simulacao.taxas")
public record TaxasProperties(
        BigDecimal taxaRealAnualPadrao,
        Duration cacheTtl
) {
    public TaxasProperties {
        if (taxaRealAnualPadrao == null) {
            taxaRealAnualPadrao = new BigDecimal("0.04");
        }
        if (cacheTtl == null) {
            cacheTtl = Duration.ofHours(24);
        }
    }
}

