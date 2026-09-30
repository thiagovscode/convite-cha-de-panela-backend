package br.com.convite.exception;

public class ConviteNaoEncontradoException extends EntidadeNaoEncontradaException {

    public ConviteNaoEncontradoException() {
        super("Convite não localizado na lista oficial de convidados.");
    }

    public ConviteNaoEncontradoException(String codigo) {
        super("Convite não localizado na lista de convidados: " + codigo);
    }
}
