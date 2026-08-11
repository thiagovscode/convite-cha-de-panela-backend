package br.com.convite.gateway.persistence;

import br.com.convite.gateway.persistence.entity.ReservaEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReservaRepository extends MongoRepository<ReservaEntity, String> {
    List<ReservaEntity> findByPresenteId(Long presenteId);
}
