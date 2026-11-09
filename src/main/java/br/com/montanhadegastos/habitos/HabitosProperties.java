package br.com.montanhadegastos.habitos;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.List;

@ConfigurationProperties(prefix = "simulacao")
public record HabitosProperties(List<Habito> habitos) {

    public HabitosProperties {
        if (habitos == null || habitos.isEmpty()) {
            habitos = padrao();
        } else {
            habitos = List.copyOf(habitos);
        }
    }

    public static List<Habito> padrao() {
        return List.of(
                new Habito("CAFE", "Café", new BigDecimal("8.00"), Frequencia.DIA, "cafés"),
                new Habito("DELIVERY", "Delivery", new BigDecimal("45.00"), Frequencia.SEMANA, "pedidos"),
                new Habito("FUMO", "Fumo", new BigDecimal("12.00"), Frequencia.DIA, "maços de cigarro"),
                new Habito("COMPRAS", "Compras", new BigDecimal("300.00"), Frequencia.MES, "compras")
        );
    }
}

