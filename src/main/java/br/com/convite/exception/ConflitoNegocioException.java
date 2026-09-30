package br.com.convite.exception;

public class ConflitoNegocioException extends RuntimeException {

    public ConflitoNegocioException() {
        super("Conflito na operação solicitada.");
    }

    public ConflitoNegocioException(String message) {
        super(message);
    }
}
