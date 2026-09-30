package br.com.convite.gateway.persistence;

import br.com.convite.gateway.persistence.entity.ConviteCasamentoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConviteCasamentoRepository extends MongoRepository<ConviteCasamentoEntity, String> {
    Optional<ConviteCasamentoEntity> findByCodigoIgnoreCase(String codigo);

    @Query("{ '$or': [ " +
           "{ 'familia': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'codigo': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'telefone': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'membros.nome': { '$regex': ?0, '$options': 'i' } } " +
           "] }")
    List<ConviteCasamentoEntity> buscarPorTermoGeral(String termo);

    /** Busca convites que possuem um membro com o nome informado (case-insensitive, busca exata no array) */
    @Query("{ 'membros.nome': { '$regex': ?0, '$options': 'i' } }")
    List<ConviteCasamentoEntity> findByMembrosNomeRegex(String nomeRegex);
}

