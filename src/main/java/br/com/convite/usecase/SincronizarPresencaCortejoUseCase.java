package br.com.convite.usecase;

import br.com.convite.domain.MembroConvite;

import java.time.LocalDateTime;

public interface SincronizarPresencaCortejoUseCase {
    void executar(String codigoConvite, MembroConvite membro, boolean presente, LocalDateTime agora);
}
