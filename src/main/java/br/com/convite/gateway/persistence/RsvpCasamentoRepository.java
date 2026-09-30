package br.com.convite.gateway.persistence;

import br.com.convite.gateway.persistence.entity.RsvpCasamentoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RsvpCasamentoRepository extends MongoRepository<RsvpCasamentoEntity, String> {
    Optional<RsvpCasamentoEntity> findFirstByTelefone(String telefone);
    Optional<RsvpCasamentoEntity> findByTelefone(String telefone);
}