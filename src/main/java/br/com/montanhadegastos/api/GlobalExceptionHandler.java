package br.com.montanhadegastos.api;

import br.com.montanhadegastos.habitos.HabitoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();
        String mensagem = detalhes.isEmpty() ? "Dados de entrada inválidos" : detalhes.getFirst();
        ErroResposta corpo = new ErroResposta(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDACAO_INVALIDA",
                mensagem,
                detalhes
        );
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(HabitoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarHabitoNaoEncontrado(HabitoNaoEncontradoException ex) {
        ErroResposta corpo = new ErroResposta(
                HttpStatus.BAD_REQUEST.value(),
                "HABITO_INEXISTENTE",
                ex.getMessage(),
                List.of(ex.getMessage())
        );
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarJsonMalformado(HttpMessageNotReadableException ex) {
        String mensagem = "Corpo da requisição inválido ou malformado (verifique o JSON e a frequência informada)";
        ErroResposta corpo = new ErroResposta(
                HttpStatus.BAD_REQUEST.value(),
                "REQUISICAO_MALFORMADA",
                mensagem,
                List.of(mensagem)
        );
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarErroInesperado(Exception ex) {
        String mensagem = "Erro interno inesperado ao processar a solicitação";
        ErroResposta corpo = new ErroResposta(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "ERRO_INTERNO",
                mensagem,
                List.of(mensagem)
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(corpo);
    }
}

