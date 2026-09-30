package br.com.convite.exception;

public class CheckinDuplicadoException extends ConflitoNegocioException {
    public CheckinDuplicadoException() {
        super("Entrada já registrada anteriormente para todos os membros deste convite. Possível duplicata de QR Code.");
    }

    public CheckinDuplicadoException(String message) {
        super(message);
    }
}
