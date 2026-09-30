package br.com.convite.exception;

public class ParticipanteNaoEncontradoException extends EntidadeNaoEncontradaException {
    public ParticipanteNaoEncontradoException() {
        super("Participante da cerimônia não localizado.");
    }

    public ParticipanteNaoEncontradoException(String id) {
        super("Participante da cerimônia não localizado: " + id);
    }
}
