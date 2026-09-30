package br.com.convite.exception;

public class EntidadeNaoEncontradaException extends RuntimeException {

    public EntidadeNaoEncontradaException() {
        super("Recurso solicitado não foi encontrado.");
    }

    public EntidadeNaoEncontradaException(String message) {
        super(message);
    }
}
