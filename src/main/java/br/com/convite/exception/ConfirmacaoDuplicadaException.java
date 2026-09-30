package br.com.convite.exception;

public class ConfirmacaoDuplicadaException extends ConflitoNegocioException {
    public ConfirmacaoDuplicadaException() {
        super("Este convite já foi confirmado anteriormente. Para alterações, entre em contato diretamente com os noivos.");
    }

    public ConfirmacaoDuplicadaException(String message) {
        super(message);
    }
}
