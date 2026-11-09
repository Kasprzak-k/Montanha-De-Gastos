package br.com.montanhadegastos.taxas;

public class FalhaConsultaBcbException extends RuntimeException {

    public FalhaConsultaBcbException(String message) {
        super(message);
    }

    public FalhaConsultaBcbException(String message, Throwable cause) {
        super(message, cause);
    }
}

