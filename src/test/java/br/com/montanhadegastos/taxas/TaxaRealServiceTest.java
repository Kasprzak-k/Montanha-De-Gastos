package br.com.montanhadegastos.taxas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaxaRealServiceTest {

    private static class MutableClock extends Clock {
        private Instant instant;

        MutableClock(Instant initial) {
            this.instant = initial;
        }

        void advance(Duration duration) {
            this.instant = this.instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }

    @Test
    @DisplayName("Segunda chamada dentro de 24h deve usar o cache sem chamar o cliente novamente")
    void segundaChamadaDeveUsarCache() {
        BcbSgsClient client = mock(BcbSgsClient.class);
        when(client.buscarTaxasAnuais()).thenReturn(
                new TaxasAnuaisBcb(new BigDecimal("0.0816"), new BigDecimal("0.0400"))
        );
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        TaxasProperties props = new TaxasProperties(new BigDecimal("0.04"), Duration.ofHours(24));
        TaxaRealService service = new TaxaRealService(client, props, clock);

        TaxaRealVigente primeira = service.obterTaxaReal();
        clock.advance(Duration.ofHours(12));
        TaxaRealVigente segunda = service.obterTaxaReal();

        assertEquals(new BigDecimal("0.04000000"), primeira.taxaRealAnual());
        assertEquals("bcb", primeira.origem());
        assertEquals(primeira, segunda);
        verify(client, times(1)).buscarTaxasAnuais();
    }

    @Test
    @DisplayName("Cache expirado após 24h deve buscar novamente no cliente")
    void cacheExpiradoDeveBuscarNovamente() {
        BcbSgsClient client = mock(BcbSgsClient.class);
        when(client.buscarTaxasAnuais())
                .thenReturn(new TaxasAnuaisBcb(new BigDecimal("0.0816"), new BigDecimal("0.0400")))
                .thenReturn(new TaxasAnuaisBcb(new BigDecimal("0.1025"), new BigDecimal("0.0500")));
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        TaxasProperties props = new TaxasProperties(new BigDecimal("0.04"), Duration.ofHours(24));
        TaxaRealService service = new TaxaRealService(client, props, clock);

        TaxaRealVigente primeira = service.obterTaxaReal();
        clock.advance(Duration.ofHours(24).plusSeconds(1));
        TaxaRealVigente segunda = service.obterTaxaReal();

        assertEquals(new BigDecimal("0.04000000"), primeira.taxaRealAnual());
        assertEquals(new BigDecimal("0.05000000"), segunda.taxaRealAnual());
        assertEquals("bcb", segunda.origem());
        verify(client, times(2)).buscarTaxasAnuais();
    }

    @Test
    @DisplayName("Quando a API do BCB estiver fora do ar deve usar fallback e marcar origem 'padrao'")
    void apiForaDoArDeveUsarFallbackPadrao() {
        BcbSgsClient client = mock(BcbSgsClient.class);
        when(client.buscarTaxasAnuais()).thenThrow(new FalhaConsultaBcbException("Timeout BCB"));
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        TaxasProperties props = new TaxasProperties(new BigDecimal("0.04"), Duration.ofHours(24));
        TaxaRealService service = new TaxaRealService(client, props, clock);

        TaxaRealVigente resultado = service.obterTaxaReal();

        assertEquals(new BigDecimal("0.04000000"), resultado.taxaRealAnual());
        assertEquals(new BigDecimal("0.00327374"), resultado.taxaRealMensal());
        assertEquals("padrao", resultado.origem());
    }
}

