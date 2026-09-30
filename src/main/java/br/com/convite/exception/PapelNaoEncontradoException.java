package br.com.convite.exception;

public class PapelNaoEncontradoException extends EntidadeNaoEncontradaException {

    public PapelNaoEncontradoException() {
        super("Papel não localizado no sistema.");
    }

    public PapelNaoEncontradoException(String id) {
        super("Papel não localizado: " + id);
    }
}
