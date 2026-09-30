package br.com.convite.gateway.persistence;

import br.com.convite.gateway.persistence.entity.ParticipanteCerimoniaEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipanteCerimoniaRepository extends MongoRepository<ParticipanteCerimoniaEntity, String> {
    List<ParticipanteCerimoniaEntity> findByPapelIgnoreCase(String papel);
    Optional<ParticipanteCerimoniaEntity> findFirstByNomeIgnoreCase(String nome);
    Optional<ParticipanteCerimoniaEntity> findByNomeIgnoreCase(String nome);
    List<ParticipanteCerimoniaEntity> findByCodigoConviteIgnoreCase(String codigoConvite);
}
