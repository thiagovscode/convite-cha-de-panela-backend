package br.com.convite.exception;

public class ConvidadoNaoEncontradoException extends EntidadeNaoEncontradaException {

    public ConvidadoNaoEncontradoException() {
        super("Convidado não pertence a este convite");
    }

    public ConvidadoNaoEncontradoException(String identificador) {
        super("Convidado não pertence a este convite: " + identificador);
    }
}
