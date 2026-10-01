package br.com.convite.gateway.persistence;

import br.com.convite.gateway.persistence.entity.ConfiguracaoEventoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracaoEventoRepository extends MongoRepository<ConfiguracaoEventoEntity, String> {
    Optional<ConfiguracaoEventoEntity> findByChave(String chave);
}
