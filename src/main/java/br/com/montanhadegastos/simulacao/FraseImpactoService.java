package br.com.montanhadegastos.simulacao;

import br.com.montanhadegastos.habitos.Habito;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;

@Service
public class FraseImpactoService {

    private static final Locale LOCALE_BR = Locale.of("pt", "BR");
    private static final DecimalFormatSymbols SYMBOLS_BR = DecimalFormatSymbols.getInstance(LOCALE_BR);

    private static final String TEMPLATE_PADRAO =
            "Em %d %s, seus %s %s custam R$ %s e, investidos, virariam R$ %s.";

    private static final Map<String, String> TEMPLATES_POR_HABITO = Map.of(
            "CAFE", "Em %d %s, seus %s %s custam R$ %s e, investidos, virariam R$ %s.",
            "DELIVERY", "Em %d %s, seus %s %s no delivery custam R$ %s e, investidos, virariam R$ %s.",
            "FUMO", "Em %d %s, seus %s %s custam R$ %s e, investidos, virariam R$ %s.",
            "COMPRAS", "Em %d %s, suas %s %s custam R$ %s e, investidas, virariam R$ %s."
    );

    public String gerarFrase(
            Habito habito,
            int anos,
            BigDecimal custoBruto,
            long quantidadeItens,
            BigDecimal montanteInvestido
    ) {
        String idHabito = habito != null && habito.id() != null ? habito.id().toUpperCase(Locale.ROOT) : "";
        String rotulo = habito != null && habito.rotuloItem() != null ? habito.rotuloItem() : "itens";
        String template = TEMPLATES_POR_HABITO.getOrDefault(idHabito, TEMPLATE_PADRAO);
        String unidadeAnos = anos == 1 ? "ano" : "anos";

        return String.format(
                LOCALE_BR,
                template,
                anos,
                unidadeAnos,
                formatarInteiro(quantidadeItens),
                rotulo,
                formatarMoeda(custoBruto),
                formatarMoeda(montanteInvestido)
        );
    }

    String formatarInteiro(long valor) {
        DecimalFormat df = new DecimalFormat("#,##0", SYMBOLS_BR);
        return df.format(valor);
    }

    String formatarMoeda(BigDecimal valor) {
        BigDecimal arredondado = valor.setScale(2, RoundingMode.HALF_UP);
        boolean semCentavos = arredondado.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
        DecimalFormat df = new DecimalFormat(semCentavos ? "#,##0" : "#,##0.00", SYMBOLS_BR);
        return df.format(arredondado);
    }
}

