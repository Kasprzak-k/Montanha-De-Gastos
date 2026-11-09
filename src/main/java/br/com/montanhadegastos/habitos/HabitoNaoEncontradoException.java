package br.com.montanhadegastos.habitos;

public class HabitoNaoEncontradoException extends RuntimeException {

    public HabitoNaoEncontradoException(String id) {
        super("Hábito inválido ou não encontrado: " + id);
    }
}

