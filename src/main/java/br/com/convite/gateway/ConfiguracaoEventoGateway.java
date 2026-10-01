package br.com.convite.gateway;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ConfiguracaoEventoGateway {
    Optional<LocalDateTime> buscarPrazoRsvp();
    void salvarPrazoRsvp(LocalDateTime prazo);
}
