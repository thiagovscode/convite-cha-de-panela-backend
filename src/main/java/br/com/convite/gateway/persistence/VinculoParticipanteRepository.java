package br.com.convite.gateway.persistence;

import br.com.convite.gateway.persistence.entity.VinculoParticipanteEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VinculoParticipanteRepository extends MongoRepository<VinculoParticipanteEntity, String> {
    Optional<VinculoParticipanteEntity> findByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCase(String nome);
}
