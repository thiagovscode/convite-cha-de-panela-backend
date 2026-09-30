package br.com.convite.gateway.persistence;

import br.com.convite.gateway.persistence.entity.PapelParticipanteEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PapelParticipanteRepository extends MongoRepository<PapelParticipanteEntity, String> {
    Optional<PapelParticipanteEntity> findByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCase(String nome);
}
