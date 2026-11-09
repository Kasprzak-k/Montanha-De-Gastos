package br.com.montanhadegastos.api;

import java.util.List;

public record ErroResposta(
        int status,
        String erro,
        String mensagem,
        List<String> detalhes
) {
}

