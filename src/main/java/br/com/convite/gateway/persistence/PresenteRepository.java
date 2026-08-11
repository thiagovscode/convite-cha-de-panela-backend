package br.com.convite.gateway.persistence;

import br.com.convite.gateway.persistence.entity.PresenteEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PresenteRepository extends MongoRepository<PresenteEntity, Long> {
}
