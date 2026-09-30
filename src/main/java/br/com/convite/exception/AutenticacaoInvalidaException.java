package br.com.convite.exception;

public class AutenticacaoInvalidaException extends RuntimeException {
    public AutenticacaoInvalidaException() {
        super("Usuário ou senha inválidos.");
    }
}
