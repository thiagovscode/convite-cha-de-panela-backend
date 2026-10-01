package br.com.convite.usecase;

import br.com.convite.entrypoint.api.model.ConfiguracaoEventoResponse;

import java.time.LocalDateTime;

public interface AtualizarPrazoRsvpUseCase {
    ConfiguracaoEventoResponse executar(LocalDateTime novoPrazo);
}
