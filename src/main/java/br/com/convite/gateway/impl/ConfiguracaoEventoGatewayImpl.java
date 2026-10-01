package br.com.convite.gateway.impl;

import br.com.convite.gateway.ConfiguracaoEventoGateway;
import br.com.convite.gateway.persistence.ConfiguracaoEventoRepository;
import br.com.convite.gateway.persistence.entity.ConfiguracaoEventoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ConfiguracaoEventoGatewayImpl implements ConfiguracaoEventoGateway {

    private static final String CHAVE_PRINCIPAL = "principal";
    private final ConfiguracaoEventoRepository repository;

    @Override
    public Optional<LocalDateTime> buscarPrazoRsvp() {
        return repository.findByChave(CHAVE_PRINCIPAL).map(ConfiguracaoEventoEntity::getPrazoRsvp);
    }

    @Override
    public void salvarPrazoRsvp(LocalDateTime prazo) {
        ConfiguracaoEventoEntity entity = repository.findByChave(CHAVE_PRINCIPAL)
                .orElseGet(() -> ConfiguracaoEventoEntity.builder()
                        .chave(CHAVE_PRINCIPAL)
                        .build());
        entity.setPrazoRsvp(prazo);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
    }
}
