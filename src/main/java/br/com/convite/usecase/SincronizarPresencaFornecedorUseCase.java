package br.com.convite.usecase;

import br.com.convite.domain.MembroConvite;

import java.time.LocalDateTime;

public interface SincronizarPresencaFornecedorUseCase {
    void executar(MembroConvite membro, boolean presente, LocalDateTime agora);
}
