package br.com.convite.exception;

public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException() {
        super("Acesso negado. Permissão insuficiente.");
    }
}
