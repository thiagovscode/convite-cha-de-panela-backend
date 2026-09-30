package br.com.convite.usecase;

import br.com.convite.domain.Convite;

public interface AtualizarConviteUseCase {
    /**
     * Atualiza um convite existente no sistema.
     * Identifica assertivamente pelo Código + ID do convite e preserva status de RSVP e check-in dos membros.
     */
    Convite executar(Convite dados);
}
