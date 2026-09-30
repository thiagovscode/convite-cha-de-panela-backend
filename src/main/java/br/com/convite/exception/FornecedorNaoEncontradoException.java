package br.com.convite.exception;

public class FornecedorNaoEncontradoException extends EntidadeNaoEncontradaException {

    public FornecedorNaoEncontradoException() {
        super("Fornecedor não localizado.");
    }

    public FornecedorNaoEncontradoException(String id) {
        super("Fornecedor não localizado: " + id);
    }
}
