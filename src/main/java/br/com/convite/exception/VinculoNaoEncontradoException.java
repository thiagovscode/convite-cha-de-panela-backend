package br.com.convite.exception;

public class VinculoNaoEncontradoException extends EntidadeNaoEncontradaException {

    public VinculoNaoEncontradoException() {
        super("Vínculo não localizado no sistema.");
    }

    public VinculoNaoEncontradoException(String id) {
        super("Vínculo não localizado: " + id);
    }
}
