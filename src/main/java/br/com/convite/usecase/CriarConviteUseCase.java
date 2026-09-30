package br.com.convite.usecase;

import br.com.convite.domain.Convite;

public interface CriarConviteUseCase {
    /**
     * Cadastra um novo convite no sistema com validações exclusivas de criação.
     * Gera código único caso não informado e impede sobrescrita de convites existentes.
     */
    Convite executar(Convite dados);
}
